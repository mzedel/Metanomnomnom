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
	 *  5: Birth year
	 *  6: Birth month
	 *  7: Birth day
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
	 * is returned. If the record is to be split (as multiple instances are
	 * represented), the resulting array's length is a multitude of the normal
	 * length, and all records share the same ID.
	 * @param record the record
	 * @return the converted record or <tt>null</tt>, if the record shall be
	 * 	discarded
	 */
	private String[] convertRecord(String[] record) {
		String[] convertedRecord = new String[record.length + 2];
		
		// do not change the ID
		convertedRecord[0] = copyTrimmed(record[0]);
		
		// do not change the gender-specific address
		convertedRecord[1] = copyTrimmed(record[1]);
		
		// do not change the academic title
		convertedRecord[2] = copyTrimmed(record[2]);
		
		// first names
		String firstName = copyTrimmed(record[3]);
		// change "eSvn." to "Sven." - the structure of these cases always seem to be the same
		if (firstName != null && firstName.length() >= 2 && Character.isUpperCase(firstName.charAt(1))) {
			firstName = firstName.substring(1, firstName.length() >= 3 ? 3 : 2)
					.concat(firstName.substring(0, 1))
					.concat(firstName.length() >= 4 ? (firstName.substring(3)) : "");
		}
		convertedRecord[3] = firstName;
		
		// last names
		String lastName = copyTrimmed(record[4]);
		// remove everything after more than one space (e.g. "   ibn")
		if (lastName != null) {
			int crapIndex = lastName.indexOf("  ");
			if (crapIndex != -1) {
				lastName = lastName.substring(0, crapIndex);
			}
		}
		
		convertedRecord[4] = lastName;
		
		// normalize the date of birth
		String rawDate = copyTrimmed(record[5]);
		String rawDay = "";
		String rawMonth = "";
		if (rawDate != null && rawDate.length() >= 8) {
			if (rawDate.contains(".")) {
				// dd.mm.yyyy
			    rawDay = rawDate.substring(0, 2);
	            rawMonth = rawDate.substring(3, 5);
				rawDate = rawDate.substring(rawDate.length() - 4);
				if (rawDate.startsWith("20")) {
					rawDate = "19" + rawDate.substring(2);
				}
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
				// check the format
				Integer.parseInt(rawDate);
				int monthNumber = Integer.parseInt(rawMonth);
				Integer.parseInt(rawDay);
				
				// position of month vs. day
				if (monthNumber > 12) {
				  String temp = rawMonth;
				  rawMonth = rawDay;
				  rawDay = temp;
				}
				  
				// store the date
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
		
		// streets
		String street = copyTrimmed(record[6]);
		String houseNumber = null;
		if (street != null) {
			// extract house number
			int lastBlankIndex = street.lastIndexOf(" ");
			if (lastBlankIndex != -1) {
				if (street.length() > lastBlankIndex + 1) {
					String potentialNumber = street.substring(lastBlankIndex + 1);
					if (Character.isDigit(potentialNumber.charAt(0))) {
						// assume that the substring is a house number
						street = street.substring(0, lastBlankIndex);
						houseNumber = potentialNumber;
					}
				}
				
			}
			// normalize abbreviations
			street = street.replaceAll("strasse", "str.")
					.replaceAll("straﬂe", "str.")
					.replaceAll("Strasse", "Str.")
					.replaceAll("Straﬂe", "Str.");
		}
		convertedRecord[8] = street;
		
		// normalize house numbers (to lower case)
		convertedRecord[9] = copyTrimmed(record[7]);
		if (convertedRecord[9] == null) {
			// if there is no house number, use the number extracted from the street (may also be null)
			convertedRecord[9] = houseNumber;
		}
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
		
		// copy places
		convertedRecord[11] = copyTrimmed(record[9]);
		
		// copy mobile phone number
		convertedRecord[12] = copyTrimmed(record[10]);
		
		// ignore unknown and rare data
		convertedRecord[13] = null;
		
		// keep whatever number that is, for it is almost always there
		convertedRecord[14] = copyTrimmed(record[12]);
		
		// ignore unknown and rare data
		convertedRecord[15] = null;
		
		/*
		 * Check for multiple records
		 */
		// check first names (" u. ", " und ", " & ")
		firstName = convertedRecord[3];
		final int recordLength = convertedRecord.length;
		if (firstName != null) {
			for (String pattern : new String[] {" u. ", " und ", " & "}) {
				String[] firstNames = firstName.split(pattern);
				if (firstNames.length > 1) {
					// create two records
					String[] doubleRecord = new String[recordLength * 2];
					
					System.arraycopy(convertedRecord, 0, doubleRecord, 0, recordLength);
					System.arraycopy(convertedRecord, 0, doubleRecord, recordLength, recordLength);
					doubleRecord[3] = firstNames[0];
					doubleRecord[3 + recordLength] = firstNames[1];
					
					convertedRecord = doubleRecord;
					break;
				}
			}
		}
		
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
		String copy = string != null ? string.toLowerCase().trim().replaceAll("\"", "") : null; // might also strip off leading and trailing .:*-; etc.
		return !"".equals(copy) ? copy : null;
	}
	
}
