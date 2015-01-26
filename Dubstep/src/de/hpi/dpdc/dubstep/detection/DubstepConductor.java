package de.hpi.dpdc.dubstep.detection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;
import de.hpi.dpdc.dubstep.detection.address.Address;

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
		long timeTotalMillis = 0;
		long timeDiffMillis = 0;

		// read
		List<String> raw = this.dataFactory.createReader().read(this.input.toString());
		timeDiffMillis = (System.nanoTime() - timeNow) / 1000000;
		timeTotalMillis += timeDiffMillis;
		System.out.println("Reading: " + timeDiffMillis + "ms");
		timeNow = System.nanoTime();

		// parse
		List<String[]> records = this.dataFactory.createParser().parse(raw);
		raw = null;
		timeDiffMillis = (System.nanoTime() - timeNow) / 1000000;
		timeTotalMillis += timeDiffMillis;
		System.out.println("Parsing: " + timeDiffMillis + "ms");
		timeNow = System.nanoTime();

		// convert
		records = this.dataFactory.createConverter().convert(records);
		System.out.println("Converting: " + timeDiffMillis + "ms");
		timeNow = System.nanoTime();

		// split double records
		records = this.splitRecords(records);
		timeDiffMillis = (System.nanoTime() - timeNow) / 1000000;
		timeTotalMillis += timeDiffMillis;
		System.out.println("Splitting: " + timeDiffMillis + "ms");
		timeNow = System.nanoTime();

		// blocking
		Map<String, List<Address>> blocks = this.createBlocks(records);
		records = null;
		timeDiffMillis = (System.nanoTime() - timeNow) / 1000000;
		timeTotalMillis += timeDiffMillis;
		System.out.println("Blocking: " + timeDiffMillis + "ms");
		timeNow = System.nanoTime();

		// find duplicates
		Set<Duplicate> duplicates = this.findDuplicates(blocks);
		blocks = null;
		timeDiffMillis = (System.nanoTime() - timeNow) / 1000000;
		timeTotalMillis += timeDiffMillis;
		System.out.println("Finding: " + timeDiffMillis + "ms");
		System.out.println("\tnumber of duplicates: " + duplicates.size());
		timeNow = System.nanoTime();

		// write the duplicates
		this.writeDuplicates(duplicates);
		timeDiffMillis = (System.nanoTime() - timeNow) / 1000000;
		timeTotalMillis += timeDiffMillis;
		System.out.println("Writing: " + timeDiffMillis + "ms");
		System.out.println("\nTotal: " + timeTotalMillis + "ms");
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

	/**
	 * Create proper {@link Address} objects from raw records, remove objects
	 * without keys (no postal code or last name), group by key.
	 * @param records the records to be grouped
	 * @return map of grouped records
	 */
	private Map<String, List<Address>> createBlocks(List<String[]> records) {
		Map<String, List<Address>> blocks = new TreeMap<String, List<Address>>();

		Address address = null;
		for (String[] record : records) {
			// create Address object from record
			address = new Address(record);
			if (address.key == null) {
				// no key (i.e., no postal code or no last name) => skip
				continue;
			}

			// add record to map
			if (!blocks.containsKey(address.key)) {
				// add new list for key
				blocks.put(address.key, new ArrayList<Address>());
			}
			blocks.get(address.key).add(address);
		}

		return blocks;
	}

	/**
	 * Convenience class for handling duplicates. The first id is always the
	 * smaller one, and <tt>Duplicate</tt>s are comparable.
	 */
	public class Duplicate implements Comparable<Duplicate> {

		private int id1;
		private int id2;

		public Duplicate(int idLeft, int idRight) {
			if (idLeft < idRight) {
				this.id1 = idLeft;
				this.id2 = idRight;
			} else if (idRight < idLeft) {
				this.id1 = idRight;
				this.id2 = idLeft;
			} else {
				// same id: we do not need trivial duplicates
				throw new RuntimeException("Tried to create trivial duplicate: " + idLeft);
			}
		}

		@Override
		public int compareTo(Duplicate other) {
			if (this.id1 < other.id1 || (this.id1 == other.id1 && this.id2 < other.id2)) {
				return -1;
			} else if (this.id1 == other.id1 && this.id2 == other.id2) {
				return 0;
			} else {
				return 1;
			}
		}

		public String toString() {
			return this.id1 + "," + this.id2;
		}

	}

	/**
	 * Find duplicates.
	 * @param blocks the blocks of records
	 * @return the set of duplicates
	 */
	private Set<Duplicate> findDuplicates(Map<String, List<Address>> blocks) throws IOException {
		// prepare sorted set of output strings ("id1,id2")
		SortedSet<Duplicate> duplicates = new TreeSet<Duplicate>();

		// find duplicates
		System.out.println("\tnumber of blocks: " + blocks.size());
		long printThreshold = blocks.size() / 10;
		long blocksDoneTotal = 0;
		
		Address address1, address2;
		for (List<Address> block : blocks.values()) {
			// compare records within the block
			int blockSize = block.size();
			for (int i = 0; i < blockSize; i++) {
				address1 = block.get(i);
				for (int j = i + 1; j < blockSize; j++) {
					address2 = block.get(j);
					if (!address1.origId.equals(address2.origId)) {	// split records have the same ID
						if (this.isDuplicate(address1, address2)) {
							// found a duplicate, add it to the collection
							duplicates.add(new Duplicate(address1.origId, address2.origId));	// ids must not be null
//							System.out.println(address1);
//							System.out.println("-----------------");
//							System.out.println(address2);
//							System.out.println("=================");
						}
					}
				}
			}
			
			printThreshold--;
			blocksDoneTotal++;
			if (printThreshold <= 0) {
				System.out.println("\t" + blocksDoneTotal + " of " + blocks.size() + " blocks processed");
				printThreshold = blocks.size() / 10;
			}
		}

		// return result
		return duplicates;
	}

	/**
	 * Write the set of duplicates to the output path.
	 * @param duplicates the duplicates
	 * @throws IOException if the writing fails
	 */
	private void writeDuplicates(Set<Duplicate> duplicates) throws IOException {
		// write duplicates to output path
		List<String> duplicateStrings = new ArrayList<String>(duplicates.size());
		for (Duplicate duplicate : duplicates) {
			duplicateStrings.add(duplicate.toString());
		}
		Files.write(this.output, duplicateStrings, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * Decide whether the given objects are duplicates, i.e., represent the same
	 * real world entity.
	 * @param address1
	 * @param address2
	 * @return <tt>true</tt> if the given objects are probably duplicates, 
	 * 	<tt>false</tt> otherwise
	 */
	private boolean isDuplicate(Address address1, Address address2) {
		AbstractStringMetric levenshteinMetric = new Levenshtein();
		AbstractStringMetric winklerMetric = new JaroWinkler();
		AbstractStringMetric mongeElkanMetric = new MongeElkan();
		// may need some adjustments 
		// used https://nats-www.informatik.uni-hamburg.de/pub/User/PhD/ElitaGavrilaVertanEditedFinal.pdf´
		// as a rule of thumb
		final double LEVENSHTEIN_THRESHOLD = 0.65;
		final double WINKLER_YEAR_THRESHOLD = 0.7;
		final double MONGE_ELKAN_THRESHOLD = 0.6;
		final double WINKLER_TITLE_THRESHOLD = 0.4;
		final int STREET_NAME_TOLERANCE = 6; // to allow str / strasse
		final int LENGTH_TOLERANCE = 3;
		final double BE_STRICTER = 0.1;

		if (((Math.abs(address1.lastName.length() - address2.lastName.length()) <= LENGTH_TOLERANCE)
				&& ((levenshteinMetric.getSimilarity(address1.lastName, address2.lastName) > LEVENSHTEIN_THRESHOLD)
						|| (mongeElkanMetric.getSimilarity(address1.lastName, address2.lastName) > MONGE_ELKAN_THRESHOLD)))
						|| (((address2.firstName.length() >= 2 && address1.firstName.startsWith(address2.firstName.substring(0, 2)))
								|| (address1.firstName.length() >= 2 && address2.firstName.startsWith(address1.firstName.substring(0, 2)))))) {
			if ((Math.abs(address1.firstName.length() - address2.firstName.length()) <= LENGTH_TOLERANCE) || (address1.firstName.startsWith(address2.firstName.substring(0, Math.min(2, address2.firstName.length())))
					|| address2.firstName.startsWith(address1.firstName.substring(0, Math.min(2, address1.firstName.length()))))) {
				// firstName might fit, city and streetname + birthYear should be enough
				if ((levenshteinMetric.getSimilarity(address1.firstName, address2.firstName) > LEVENSHTEIN_THRESHOLD) 
						|| (mongeElkanMetric.getSimilarity(address1.firstName, address2.firstName) > MONGE_ELKAN_THRESHOLD)) {
					// t,fN,lN,bY,sN,c
					// -,x ,x , -, -,-
					if ((Math.abs(address1.city.length() - address2.city.length()) <= LENGTH_TOLERANCE)
							&& ((levenshteinMetric.getSimilarity(address1.city, address2.city) > LEVENSHTEIN_THRESHOLD)
									|| (mongeElkanMetric.getSimilarity(address1.city, address2.city) > MONGE_ELKAN_THRESHOLD))) {
						// t,fN,lN,bY,sN,c
						// -, x, x, -, -,x
						if ((Math.abs(address1.streetName.length() - address2.streetName.length()) <= STREET_NAME_TOLERANCE)
								&& ((levenshteinMetric.getSimilarity(address1.streetName, address2.streetName) > LEVENSHTEIN_THRESHOLD)
										|| (mongeElkanMetric.getSimilarity(address1.streetName, address2.streetName) > MONGE_ELKAN_THRESHOLD))) {
							// t,fN,lN,bY,sN,c
							// -,x ,x , -, x,x
							if (address1.birthYear != null && address2.birthYear != null) {
								// if birthyears exist but are different, these might be father & son
								if (winklerMetric.getSimilarity(address1.birthYear, address2.birthYear) > WINKLER_YEAR_THRESHOLD) {
									return true;
								} else {
									return false;
								}
							}
							return true;
						}
					} else if ((address1.city.isEmpty() || address2.city.isEmpty()) && winklerMetric.getSimilarity(address1.birthYear, address2.birthYear) > WINKLER_YEAR_THRESHOLD) {
						// postalcode should be close to equal anyway, birthdates probably close
						if ((Math.abs(address1.streetName.length() - address2.streetName.length()) <= STREET_NAME_TOLERANCE)
								&& levenshteinMetric.getSimilarity(address1.streetName, address2.streetName) > LEVENSHTEIN_THRESHOLD) {
							return true;
						}
					}
				}

			}
		} else if ((levenshteinMetric.getSimilarity(address1.firstName, address2.firstName) > LEVENSHTEIN_THRESHOLD + BE_STRICTER) 
				|| (mongeElkanMetric.getSimilarity(address1.firstName, address2.firstName) > MONGE_ELKAN_THRESHOLD + BE_STRICTER)) {
			// lastName failed
			
			// t,fN,lN,bY,sN,c
			// -, X,! , -, -,-
			if ((Math.abs(address1.city.length() - address2.city.length()) <= LENGTH_TOLERANCE)
					&& ((levenshteinMetric.getSimilarity(address1.city, address2.city) > LEVENSHTEIN_THRESHOLD + BE_STRICTER)
							|| (mongeElkanMetric.getSimilarity(address1.city, address2.city) > MONGE_ELKAN_THRESHOLD + BE_STRICTER))) {
				// t,fN,lN,bY,sN,c
				// -, X,! , -, -,X
				if ((Math.abs(address1.streetName.length() - address2.streetName.length()) <= STREET_NAME_TOLERANCE)
						&& ((levenshteinMetric.getSimilarity(address1.streetName, address2.streetName) > LEVENSHTEIN_THRESHOLD)
								|| (mongeElkanMetric.getSimilarity(address1.streetName, address2.streetName) > MONGE_ELKAN_THRESHOLD))) {
					// t,fN,lN,bY,sN,c
					// -, X,! , -, x,X
					if (!address1.birthYear.isEmpty() && !address2.birthYear.isEmpty()) {
						// if birthday is not defined, we may not know, otherwise beStricter!
						if (address1.birthYear.equals(address2.birthYear)) {
							return true;
						} else {
							return false;
						}
					}
					return true;
				}
			} else if ((address1.city.isEmpty() || address2.city.isEmpty()) && winklerMetric.getSimilarity(address1.birthYear, address2.birthYear) > WINKLER_YEAR_THRESHOLD) {
				// postalcode should be close to equal anyway, birthdates probably close
				if ((Math.abs(address1.streetName.length() - address2.streetName.length()) <= STREET_NAME_TOLERANCE)
						&& levenshteinMetric.getSimilarity(address1.streetName, address2.streetName) > LEVENSHTEIN_THRESHOLD) {
					return true;
				}
			}
		} else if ((mongeElkanMetric.getSimilarity(address1.lastName, address2.lastName) > 0.8) 
				&&
				(((Math.abs(address1.city.length() - address2.city.length()) <= LENGTH_TOLERANCE)
        				&& ((levenshteinMetric.getSimilarity(address1.city, address2.city) > LEVENSHTEIN_THRESHOLD + BE_STRICTER)
        					|| (mongeElkanMetric.getSimilarity(address1.city, address2.city) > MONGE_ELKAN_THRESHOLD + BE_STRICTER)))
        			|| 
        			((!address1.title.isEmpty() && !address2.title.isEmpty())
        				&& (winklerMetric.getSimilarity(address1.title, address2.title) > WINKLER_TITLE_THRESHOLD)))) {
			// now pay extra attention to other details - 
			// t,fN,lN,bY,sN,c
			// -,! ,! , -, -,-
			if (winklerMetric.getSimilarity(address1.houseNumber, address2.houseNumber) > MONGE_ELKAN_THRESHOLD) {
				// t,fN,lN,bY,sN,c
				// y,! ,! , -, -,Y
				if (!address1.birthYear.isEmpty() && !address2.birthYear.isEmpty()) {
					if (winklerMetric.getSimilarity(address1.birthYear, address2.birthYear) > WINKLER_YEAR_THRESHOLD) {
						return true;
					} else {
						return false;
					}
				}
				if (mongeElkanMetric.getSimilarity(address1.streetName, address2.streetName) > WINKLER_YEAR_THRESHOLD) {
					return true;
				}
			}
		}

		return false;
	}

}
