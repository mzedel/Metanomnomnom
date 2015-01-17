package de.hpi.dpdc.dubstep.detection.address;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.hpi.dpdc.dubstep.detection.DataParser;

/**
 * Specific parser to deal with addresses.csv.
 */
public class AddressDataParser implements DataParser {

	private final static String SEPARATOR = ",";
	private final static int COLUMNS = 14;
	
	@Override
	public List<String[]> parse(List<String> lines) {
		List<String[]> records = new ArrayList<String[]>(lines.size());
		
		String[] record = null;
		StringTokenizer tok = null;
		int columnIndex = 0;
		String token = null;
		for (String line : lines) {
			// empty line: skip
			if (line == null || line.equals("")) continue;
			
			// parse CSV data
			record = new String[COLUMNS];
			
			// prepare to parse next line
			tok = new StringTokenizer(line, SEPARATOR, true);
			columnIndex = 0;
			
			while (columnIndex < COLUMNS) {
				// parse next column (
				token = tok.hasMoreTokens() ? tok.nextToken() : SEPARATOR;
				record[columnIndex] = !token.equals(SEPARATOR) ? token : null;
				if (!token.equals(SEPARATOR) && tok.hasMoreTokens()) tok.nextToken();
						
				columnIndex++;
			}
			
			records.add(record);
		}
		
		return records;
	}

}
