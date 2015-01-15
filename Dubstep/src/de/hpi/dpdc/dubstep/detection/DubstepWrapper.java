package de.hpi.dpdc.dubstep.detection;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Deals with I/O and stuff, thus encapsulating the actual duplicate detection
 * algorithm.
 */
public class DubstepWrapper {

	private FileReader input;
	private PrintWriter output;
	
	/**
	 * Private constructor. Usage {@link #forFiles(File, File)} to create a new
	 * instance.
	 * @param input
	 * @param output
	 */
	private DubstepWrapper(FileReader input, PrintWriter output) {
		this.input = input;
		this.output = output;
	}
	
	public static DubstepWrapper forFiles(File inputFile, File outputFile) throws IOException {
		prepareFiles(inputFile, outputFile);
		
		FileReader input = new FileReader(inputFile);
		PrintWriter output = new PrintWriter(outputFile);
		
		return new DubstepWrapper(input, output);
	}
	
	private static void prepareFiles(File inputFile, File outputFile) {
		// check input file
		if (!inputFile.exists()) {
			throw new IllegalArgumentException("Input File does not exist.");
		} else if (!inputFile.canRead()) {
			throw new IllegalArgumentException("Cannot read input file.");
		}

		// attempt to create output file (delete existing, if necessary)
		if (!outputFile.exists()) {
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
	
	public void execute() throws IOException {
		// TODO implement
		
		this.input.close();
		this.output.flush();
		this.output.close();
	}
	
}
