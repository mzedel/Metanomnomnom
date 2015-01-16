package de.hpi.dpdc.dubstep.midi;

import java.io.InputStream;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;

public final class MidiPlayer {
	
	/**
	 * The sequencer which is used to play the midi.
	 */
	private Sequencer sequencer;
	
	/**
	 * Create a new player.
	 */
	public MidiPlayer() {}
	
	/**
	 * Plays a midi file located in "files/" relative to MidiPlayer.class.
	 * If the file does not have a file type, ".mid" will be appended.
	 * This uses a separate thread. Call {@link #stop()} to stop the midi.
	 * @param name the name of the midi file
	 * @param loop whether the midi should be looped
	 */
	public void start(String name, boolean loop) {
		Thread midiThread = new Thread() {
			public void run() {
				try {
					String fileName = name.contains(".") ? name : (name + ".mid");
					
					sequencer = MidiSystem.getSequencer();
					if (loop) {
						sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
					}
					sequencer.open();
					
					InputStream stream = MidiPlayer.class.getResourceAsStream("files/" + fileName);
					sequencer.setSequence(stream);

					sequencer.addMetaEventListener(new MetaEventListener() {
						@Override
						public void meta(MetaMessage event) {
							if (event.getType() == 47) {	// 47 = end of stream
								sequencer.stop();
								sequencer.close();
							}
						}
					});
					
					sequencer.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		midiThread.setDaemon(true);	// should not stop process termination
		midiThread.start();
	}
	
	/**
	 * Stop playing the midi.
	 */
	public void stop() {
		if (this.sequencer != null && this.sequencer.isRunning()) {
			this.sequencer.stop();
		}
		if (this.sequencer != null && this.sequencer.isOpen()) {
			this.sequencer.close();
		}
	}
	
}
