package de.hpi.dpdc.dubstep.detection;

import java.util.List;

/**
 * Parse data from raw strings.
 */
public interface DataParser {

	/**
	 * Parse the given lines. Create one record (i.e., String array) for each 
	 * line. Each record is guaranteed to have an entry per column (<tt>null</tt>
	 * if no data is present).
	 * @param lines the raw data
	 * @return the parsed data records
	 */
	public List<String[]> parse(List<String> lines);
	
}
