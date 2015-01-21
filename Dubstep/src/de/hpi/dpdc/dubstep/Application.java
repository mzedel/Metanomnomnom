package de.hpi.dpdc.dubstep;

import java.io.File;
import java.io.IOException;

import de.hpi.dpdc.dubstep.detection.DubstepConductor;
import de.hpi.dpdc.dubstep.detection.address.AddressDataFactory;
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
	 * The file name of the midi file which will be played during the execution
	 * of the duplicate detection. If the file type is omitted, ".mid" will be
	 * presumed.
	 */
	private final static String MIDI_FILE_NAME = "skrillexRightIn";
	
	/**
	 * The player used to play the midi.
	 */
	private MidiPlayer midiPlayer;
	
	/**
	 * Create a new application.
	 */
	private Application() {
		this.midiPlayer = new MidiPlayer();
	}
	
	/**
	 * Start both the music and the algorithm and deal with errors.
	 * @param args program arguments, must be exactly one (input file path)
	 */
	public static void main(String[] args) {
		try {
			checkArguments(args);
			
			Application application = new Application();
			application.startMidiPlayer();
			application.executeDuplicateDetection(args[0]);
			application.stopMidiPlayer();
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
	
	/**
	 * Start the midi player.
	 */
	private void startMidiPlayer() {
		this.midiPlayer.start(Application.MIDI_FILE_NAME, true);
	}
	
	/**
	 * Stop the midi player.
	 */
	private void stopMidiPlayer() {
		this.midiPlayer.stop();
	}
	
	/**
	 * Start the duplicate detection algorithm with the given input file and an
	 * output file (not yet created) in the same directory as the input file.
	 * @param inputFilePath the path to the input file
	 * @throws IOException if such an exception occurs during the execution of
	 * 	the algorithm
	 */
	private void executeDuplicateDetection(String inputFilePath) throws IOException {
		File inputFile = new File(inputFilePath).getAbsoluteFile();
		File outputFile = new File(inputFile.getParent() + File.separator + Application.OUTPUT_FILE_NAME);
		
		DubstepConductor.create(inputFile.toPath(), outputFile.toPath(), AddressDataFactory.getInstance()).execute();
	}

}
