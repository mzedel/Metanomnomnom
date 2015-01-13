package de.hpi.dpdc.dubstep.midi;

import java.io.InputStream;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;

public final class MidiPlayer {
	
	/**
	 * Plays a midi file located in "files/" relative to MidiPlayer.class.
	 * If the file does not have a file type, ".mid" will be appended.
	 * @param name
	 * @param loop
	 */
	public static void playMidi(String name, boolean loop) {
		try {
			if (!name.contains(".")) name = name + ".mid";
			
			Sequencer sequencer = MidiSystem.getSequencer();
			if (loop) sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			sequencer.open();
			
			InputStream stream = MidiPlayer.class.getResourceAsStream("files/" + name);
			sequencer.setSequence(stream);

			sequencer.addMetaEventListener(new MetaEventListener() {
				@Override
				public void meta(MetaMessage event) {
					if (event.getType() == 47) {	// end of stream
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
	
}
