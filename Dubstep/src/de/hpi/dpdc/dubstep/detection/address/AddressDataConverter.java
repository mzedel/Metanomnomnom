package de.hpi.dpdc.dubstep.detection.address;

import java.util.ArrayList;
import java.util.List;

import de.hpi.dpdc.dubstep.detection.DataConverter;

/**
 * Normalize and clean the data from addresses.csv.
 */
public class AddressDataConverter implements DataConverter {

	/*
	 * Schema for addresses.csv:
	 * 
	 *  0: ID
	 *  1: Gender-specific title
	 *  2: Academic title
	 *  3: First name
	 *  4: Last name
	 *  5: Birth date
	 *  6: Street
	 *  7: House number
	 *  8: Postal code
	 *  9: City
	 * 10: ? (Mobile phone?)
	 * 11: ?
	 * 12: ? (usually present)
	 * 13: ?
	 */
	
	@Override
	public List<String[]> convert(List<String[]> records) {
		List<String[]> convertedRecords = new ArrayList<String[]>(records.size());
		
		/*
		 * Convert each record one by one.
		 * If null is returned, the record will be discarded (i.e., not be added
		 * to the result list).
		 */
		for (String[] record : records) {
			String[] convertedRecord = this.convertRecord(record);
			if (convertedRecord != null) {
				convertedRecords.add(convertedRecord);
			}
		}
		
		return convertedRecords;
	}
	
	/**
	 * Convert the given record. If the record is to be discarded, <tt>null</tt>
	 * is returned.
	 * @param record the record
	 * @return the converted record or <tt>null</tt>, if the record shall be
	 * 	discarded
	 */
	private String[] convertRecord(String[] record) {
		String[] convertedRecord = new String[record.length + 2];
		
		// do not change the ID
		convertedRecord[0] = copyTrimmed(record[0]);
		
		// TODO split records for "Herr und Frau" / "Frau und Herr"
		convertedRecord[1] = copyTrimmed(record[1]);
		
		// do not change the academic title
		convertedRecord[2] = copyTrimmed(record[2]);
		
		// TODO deal with first names (e.g. abbreviations, double names, "eSvn.")
		convertedRecord[3] = copyTrimmed(record[3]);
		
		// TODO deal with last names (e.g. remove everything after more than one space)
		convertedRecord[4] = copyTrimmed(record[4]);
		
		// extract the year from the date of birth
		String rawDate = copyTrimmed(record[5]);
		String rawDay = "";
		String rawMonth = "";
		if (rawDate != null && rawDate.length() >= 8) {
			if (rawDate.contains(".")) {
				// dd.mm.yyyy
			    rawDay = rawDate.substring(0, 2);
	            rawMonth = rawDate.substring(3, 5);
				rawDate = rawDate.substring(rawDate.length() - 4);
				if (rawDate.startsWith("20"))
				  rawDate = "19" + rawDate.substring(2);
			} else if (rawDate.contains("/")) {
				// ??/??/yy
                rawMonth = rawDate.substring(0, 2);
                rawDay = rawDate.substring(3, 5);
				rawDate = "19" + rawDate.substring(rawDate.length() - 2);
			} else {
				// yyyy????
                rawDay = rawDate.substring(rawDate.length() - 2);
                rawMonth = rawDate.substring(4, 6);
				rawDate = rawDate.substring(0, 4);
			}
			try {
				// make sure that it is a number at least
				Integer.parseInt(rawDate);
				if (Integer.parseInt(rawMonth) > 12) {
				  String temp = rawMonth;
				  rawMonth = rawDay;
				  rawDay = temp;
				}
				  
				// store the year
				convertedRecord[5] = rawDate;
				convertedRecord[6] = rawMonth;
				convertedRecord[7] = rawDay;
			} catch (NumberFormatException e) {
				// not a number
				convertedRecord[5] = convertedRecord[6] = convertedRecord[7] = null;
			}
		} else {
			// null or unexpected format
		    convertedRecord[5] = convertedRecord[6] = convertedRecord[7] = null;
		}
		
		// TODO deal with streets (extract house number, normalize abbreviations)
		convertedRecord[8] = copyTrimmed(record[6]);
		
		// normalize house numbers (to lower case)
		convertedRecord[9] = copyTrimmed(record[7]);
		if (convertedRecord[9] != null) {
			convertedRecord[9] = convertedRecord[9].toLowerCase();
		}
		
		// normalize postal code (fill with leading zeros, remove "D-")
		String rawPostal = copyTrimmed(record[8]);
		if (rawPostal != null) {
			rawPostal = rawPostal.toUpperCase().replace("D-", "");
			while (rawPostal.length() < 5) {
				rawPostal = "0".concat(rawPostal);
			}
			// store the postal code
			convertedRecord[10] = rawPostal;
		} else {
			convertedRecord[10] = null;
		}
		
		// TODO deal with places (e.g. "Bunsoh , Dithm")
		convertedRecord[11] = copyTrimmed(record[9]);
		
		// TODO normalize or keep mobile phone
		convertedRecord[12] = copyTrimmed(record[10]);
		
		// ignore unknown and rare data
		convertedRecord[13] = null;
		
		// keep whatever number that is
		convertedRecord[14] = copyTrimmed(record[12]);
		
		// ignore unknown and rare data
		convertedRecord[15] = null;
		
		return convertedRecord;
	}
	
	/**
	 * Return a copy of the given string without leading / trailing whitespace /
	 * quotation marks, or <tt>null</tt>, if the given string was <tt>null</tt> 
	 * to begin with or it is an empty string after the trimming.
	 * @param string the string to be copied, can be <tt>null</tt>
	 * @return the resulting string, can be <tt>null</tt>
	 */
	private String copyTrimmed(String string) {
		String copy = string != null ? string.trim().replaceAll("\"", "") : null; // might also strip off leading and trailing .:*-; etc.
		return !"".equals(copy) ? copy : null;
	}
	
}
