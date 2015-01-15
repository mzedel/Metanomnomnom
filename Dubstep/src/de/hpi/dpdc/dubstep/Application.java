package de.hpi.dpdc.dubstep;

import java.io.File;
import java.io.IOException;

import de.hpi.dpdc.dubstep.detection.DubstepWrapper;
import de.hpi.dpdc.dubstep.midi.MidiPlayer;

/**
 * Usage:
 * 
 * 	java dubstep.jar path/to/input.csv
 * 
 * File results.txt will be created in the same directory as the input file.
 * If results.txt already exists, it will be replaced.
 */
public class Application {

	/**
	 * The file name of the output file of the duplicate detection algorithm.
	 * It will be located in the same directory as the input file.
	 */
	private final static String OUTPUT_FILE_NAME = "results.txt";
	
	/**
	 * Start both the music and the algorithm and deal with errors.
	 * @param args program arguments, must be exactly one (input file path)
	 */
	public static void main(String[] args) {
		try {
			checkArguments(args);
			
			executeMidiPlayer();
			executeDuplicateDetection(args[0]);
		} catch (IllegalArgumentException e) {
			exitWithMessage(e);
		} catch (Exception e) {
			exitWithError(e);
		}
	}
	
	/**
	 * Check that there is exactly one input argument (i.e., the path to the 
	 * input file).
	 * @param args the program arguments
	 */
	private static void checkArguments(String[] args) {
		if (args.length < 1) {
			throw new IllegalArgumentException("Missing Argument.");
		} else if (args.length > 1) {
			throw new IllegalArgumentException("Too many arguments.");
		}
	}
	
	/**
	 * Start the midi player in a separate thread (which is a daemon and,
	 * therefore, does not stop the process from terminating).
	 */
	private static void executeMidiPlayer() {
		Thread midiThread = new Thread() {
			public void run() {
				MidiPlayer.playMidi("skrillexRightIn", true);
			}
		};
		midiThread.setDaemon(true);	// does not stop process termination
		midiThread.start();
	}
	
	/**
	 * Start the duplicate detection algorithm with the given input file and an
	 * output file (not yet created) in the same directory as the input file.
	 * @param inputFilePath the path to the input file
	 * @throws IOException if such an exception occurs during the execution of
	 * 	the algorithm
	 */
	private static void executeDuplicateDetection(String inputFilePath) throws IOException {
		File inputFile = new File(inputFilePath).getAbsoluteFile();
		File outputFile = new File(inputFile.getParent() + File.separator + Application.OUTPUT_FILE_NAME);
		
		DubstepWrapper.forFiles(inputFile, outputFile).execute();
	}
	
	/**
	 * For expected exceptions. Show its message, inform the user about the 
	 * correct usage of the program and exit.
	 * @param e the exception
	 */
	private static void exitWithMessage(Exception e) {
		System.out.println(e.getMessage() 
				+ "\nUsage:\n\tjava dubstep.jar path/to/input.csv");
		System.exit(1);
	}
	
	/**
	 * For unexpected errors. Print the stack trace and exit.
	 * @param e the exception
	 */
	private static void exitWithError(Exception e) {
		e.printStackTrace();
		System.exit(1);
	}

}
