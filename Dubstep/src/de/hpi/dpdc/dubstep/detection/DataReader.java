package de.hpi.dpdc.dubstep.detection;

import java.io.IOException;
import java.util.List;

/**
 * Read data from a source.
 */
public interface DataReader {

	/**
	 * Read all data from the given source.
	 * @param source the source
	 * @return the read data as a list of strings
	 * @throws IOException if an error occurs
	 */
	public List<String> read(String source) throws IOException;
	
}
