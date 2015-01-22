package de.hpi.dpdc.dubstep.detection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;

import slib.sml.sm.core.measures.string.LevenshteinDistance;
import de.hpi.dpdc.dubstep.detection.address.Address;
import de.hpi.dpdc.dubstep.utils.HibernateUtil;

/**
 * Orchestrates the other components to perform the duplicate detection for a
 * given input and output file.
 */
public class DubstepConductor {
	
	/*
	 * Creation
	 */

	private Path input;
	private Path output;
	
	private DataFactory dataFactory;
	
	/**
	 * Private constructor. Usage {@link #forFiles(File, File)} to create a new
	 * instance.
	 * @param input
	 * @param output
	 */
	private DubstepConductor(Path input, Path output) {
		this.input = input;
		this.output = output;
	}
	
	/**
	 * Create a new instance for the given input and output paths. Both paths
	 * are validated and the output file is (re-)created. If the files cannot be
	 * accessed or created as necessary, an {@link IOException} is thrown.
	 * The given factory will be used to deal with the input data.
	 * @param input the path to the input file
	 * @param output the path to the output file
	 * @return the object which can be used to perform duplicate detection on the
	 * 	given file
	 * @throws IOException if the files cannot be accessed
	 */
	public static DubstepConductor create(Path input, Path output, DataFactory dataFactory) throws IOException {
		prepareFiles(input.toFile(), output.toFile());
		
		DubstepConductor conductor = new DubstepConductor(input, output);
		
		conductor.dataFactory = dataFactory;
		
		return conductor;
	}
	
	/**
	 * Make sure that the input file exists and can be read. Delete the old
	 * output file if necessary and create a new one. If something does not work,
	 * an {@link IllegalArgumentException} is thrown.
	 * @param inputFile the input file which must be existing and readable
	 * @param outputFile the output file which will be created anew
	 */
	private static void prepareFiles(File inputFile, File outputFile) {
		// check input file
		if (!inputFile.exists()) {
			throw new IllegalArgumentException("Input File does not exist.");
		} else if (!inputFile.canRead()) {
			throw new IllegalArgumentException("Cannot read input file.");
		}

		// attempt to create output file (delete existing, if necessary)
		if (outputFile.exists()) {
			boolean wasDeleted = outputFile.delete();
			if (!wasDeleted) {
				throw new IllegalArgumentException("Cannot delete existing output file.");
			}
		}
		boolean wasCreated = false;
		try {
			wasCreated = outputFile.createNewFile();
		} catch (IOException e) {
			// "wasCreated" is still false
		}
		if (!wasCreated) {
			throw new IllegalArgumentException("Cannot create output file.");
		}
	}
	
	/*
	 * Execution
	 */
	
	/**
	 * Performs the duplicate detection. Reads the input file and writes results
	 * to the output file.
	 * @throws IOException if anything goes wrong with reading or writing
	 */
	public void execute() throws IOException {
		long timeNow = System.nanoTime();
		
		// read
		List<String> raw = this.dataFactory.createReader().read(this.input.toString());
		System.out.println("Reading: " + ((System.nanoTime() - timeNow) / 1000000) + "ms");
		timeNow = System.nanoTime();
		
		// parse
		List<String[]> records = this.dataFactory.createParser().parse(raw);
		System.out.println("Parsing: " + ((System.nanoTime() - timeNow) / 1000000) + "ms");
		timeNow = System.nanoTime();
		
		// convert
		records = this.dataFactory.createConverter().convert(records);
		System.out.println("Converting: " + ((System.nanoTime() - timeNow) / 1000000) + "ms");
		timeNow = System.nanoTime();
		
		// split double records
		records = this.splitRecords(records);
		System.out.println("Splitting: " + ((System.nanoTime() - timeNow) / 1000000) + "ms");
		timeNow = System.nanoTime();
		
		// add records to the database
		this.addRecordsToDatabase(records);
		System.out.println("Writing to Database: " + ((System.nanoTime() - timeNow) / 1000000) + "ms");
		timeNow = System.nanoTime();
		
		// sort
		LinkedList<LinkedList<Address>> equivalenceClasses = this.sortAndGroupRecords();
		System.out.println("Sorting and blocking: " + ((System.nanoTime() - timeNow) / 1000000) + "ms");
		timeNow = System.nanoTime();
		
		HibernateUtil.shutdown();
	}
	
	/**
	 * Make sure that records which represent two entities are split up.
	 * @param records the records to be copied or split
	 * @return copied and split records
	 */
	private List<String[]> splitRecords(List<String[]> records) {
		if (records == null || records.size() == 0) {
			return new ArrayList<String[]>();
		}
		
		List<String[]> result = new ArrayList<String[]>((int) (records.size() * 1.1));
		
		final int normalLength = records.get(0).length;
		for (String[] record : records) {
			if (record.length == normalLength * 2) {
				// split
				result.add(java.util.Arrays.copyOfRange(record, 0, normalLength));
				result.add(java.util.Arrays.copyOfRange(record, normalLength, normalLength * 2));
			} else {
				// copy reference
				result.add(record);
			}
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	private LinkedList<LinkedList<Address>> sortAndGroupRecords() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		//    Query query = session.createQuery("FROM adresses adress ORDERBY LastName");

		Property lastName = Property.forName("Key");
		
		String count = session.createCriteria(Address.class).setProjection(
			Projections.projectionList().add( 
				Projections.distinct(
					Projections.projectionList() 
						.add(Projections.property(lastName.getPropertyName()))
				)
			)
		).list().size() + "";
		System.out.println(count);
		
		List<Address> result = session.createCriteria(Address.class)
//				.add(lastName.isNotNull())
				.addOrder(Order.asc("Key")).list();
		LevenshteinDistance levenshtein = new LevenshteinDistance(true); // true as in: normalize
		LinkedList<LinkedList<Address>> equivalenceClasses = new LinkedList<LinkedList<Address>>();
		for(Iterator<Address> it = result.iterator(); it.hasNext();) {
			Address address = it.next();
			String addressKey = address.Key.substring(address.Key.indexOf(":"));
			boolean found = false;
			for (LinkedList<Address> list : equivalenceClasses) {
				String name = list.getFirst().Key.substring(list.getFirst().Key.indexOf(":"));
				if (name.equals(addressKey)) {
					addDuplicate(address, list);
					found = true;
					break;
				}
			}
			if (!found) {
			    LinkedList<Address> addresses = new LinkedList<Address>();
			    addresses.add(address);
			    equivalenceClasses.add(addresses);
			}
		}
		System.out.println("Equivalence classes found:" + equivalenceClasses.size() + " from #records " + result.size());
		session.getTransaction().commit();
		session.close();
		
		return equivalenceClasses;
	}

	private void addRecordsToDatabase(List<String[]> records) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		for (String[] strings : records) {
			Address address = new Address(strings);
			session.persist(address);
		}
		session.getTransaction().commit(); 
		session.close();
	}

	private LinkedList<Address> addDuplicate(Address item, LinkedList<Address> equivalentItems) {
		if (item.SetAttributeCount > equivalentItems.get(0).SetAttributeCount)
			equivalentItems.add(0, item);
		else {
			equivalentItems.add(item);
		}
		return equivalentItems;
	}
}
