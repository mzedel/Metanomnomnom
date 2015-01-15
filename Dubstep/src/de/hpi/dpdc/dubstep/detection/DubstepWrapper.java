package de.hpi.dpdc.dubstep.detection;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Deals with I/O and stuff, thus encapsulating the actual duplicate detection
 * algorithm.
 */
public class DubstepWrapper {

	private Path input;
	private Path output;
	
	/**
	 * Private constructor. Usage {@link #forFiles(File, File)} to create a new
	 * instance.
	 * @param input
	 * @param output
	 */
	private DubstepWrapper(Path input, Path output) {
		this.input = input;
		this.output = output;
	}
	
	public static DubstepWrapper forPaths(Path input, Path output) throws IOException {
		prepareFiles(input.toFile(), output.toFile());
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
	
	public void execute() throws IOException {
		// TODO implement
		List<String> lines = Files.readAllLines(this.input, StandardCharsets.ISO_8859_1);
		System.out.println("Lines: " + lines.size());
		for (int i = 0; i < 10; i++) System.out.println(lines.get(i));
	}
	
}
