package de.hpi.dpdc.dubstep.detection;

import java.util.List;

/**
 * Convert (e.g. normalize, clean...) data.
 */
public interface DataConverter {

	/**
	 * Convert the given list of records. Any modification of content and 
	 * structure is allowed.
	 * @param records the data records
	 * @return the converted records
	 */
	public List<String[]> convert(List<String[]> records);
	
}
