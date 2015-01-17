package de.hpi.dpdc.dubstep.detection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
		// TODO implement
	    try {
	      Class.forName("org.h2.Driver");
	      Connection conn = DriverManager.getConnection("mem:test");
	      ResultSet blubb = conn.createStatement().executeQuery("");
	      while (blubb.next()) {
	        String result = blubb.getString(0);
	        System.out.println(result);
	      }
	      conn.close();
	    } catch (ClassNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    } catch (SQLException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    
		List<String[]> records = this.dataFactory.createConverter().convert(
				this.dataFactory.createParser().parse(
						this.dataFactory.createReader().read(
								this.input.toString()).subList(0, 160)));	// TODO parse all records
		
		records.forEach(record -> System.out.println(java.util.Arrays.toString(record)));
	}
	
}
