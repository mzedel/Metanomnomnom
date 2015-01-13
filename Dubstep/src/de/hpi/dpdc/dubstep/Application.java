package de.hpi.dpdc.dubstep;

import java.io.File;
import java.io.IOException;

import de.hpi.dpdc.dubstep.detection.DubstepWrapper;
import de.hpi.dpdc.dubstep.midi.MidiPlayer;

public class Application {

	private final static String OUTPUT_FILE_NAME = "results.txt";
	
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
	
	private static void checkArguments(String[] args) {
		String error = null;
		
		if (args.length < 1) error = "Missing Argument.";
		if (args.length > 1) error = "Too many arguments.";
		
		if (error != null) {
			throw new IllegalArgumentException(error);
		}
	}
	
	private static void executeMidiPlayer() {
		Thread midiThread = new Thread() {
			public void run() {
				MidiPlayer.playMidi("skrillexRightIn", true);
			}
		};
		midiThread.setDaemon(true);	// does not stop process termination
		midiThread.start();
	}
	
	private static void executeDuplicateDetection(String inputFilePath) throws IOException {
		File inputFile = new File(inputFilePath);
		File outputFile = new File(inputFile.getPath() + Application.OUTPUT_FILE_NAME);
		
		DubstepWrapper.forFiles(inputFile, outputFile).execute();
	}
	
	private static void exitWithMessage(Exception e) {
		System.out.println(e.getMessage() 
				+ "\nUsage:\n\tjava dubstep.jar path/to/input.csv");
		System.exit(1);
	}
	
	private static void exitWithError(Exception e) {
		e.printStackTrace();
		System.exit(1);
	}

}
