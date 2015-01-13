package de.hpi.dpdc.dubstep.detection;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class DubstepWrapper {

	private FileReader input;
	private PrintWriter output;
	
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
		String error = null;
		
		// check if input file can be read
		if (!inputFile.exists()) error = "Input File does not exist.";
		if (!inputFile.canRead()) error = "Cannot read input file.";
		// attempt to create output file (delete existing, if necessary)
		if (!outputFile.exists()) {
			boolean wasDeleted = outputFile.delete();
			if (!wasDeleted) error = "Cannot delete existing output file.";
		}
		boolean wasCreated = false;
		try {
			wasCreated = outputFile.createNewFile();
		} catch (IOException e) {
			// wasCreated is still false
		}
		if (!wasCreated) error = "Cannot create output file.";
		
		if (error != null) throw new IllegalArgumentException(error);
	}
	
	public void execute() throws IOException {
		// TODO implement
		
		this.input.close();
		this.output.flush();
		this.output.close();
	}
	
}
