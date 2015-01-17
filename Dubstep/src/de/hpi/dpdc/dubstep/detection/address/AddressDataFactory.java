package de.hpi.dpdc.dubstep.detection.address;

import de.hpi.dpdc.dubstep.detection.DataConverter;
import de.hpi.dpdc.dubstep.detection.DataFactory;
import de.hpi.dpdc.dubstep.detection.DataParser;
import de.hpi.dpdc.dubstep.detection.DataReader;

/**
 * Factory for dealing with "addresses.csv".
 */
public class AddressDataFactory implements DataFactory {

	private static AddressDataFactory SINGLETON = new AddressDataFactory();
	
	public static DataFactory getInstance() {
		return SINGLETON;
	}
	
	@Override
	public DataReader createReader() {
		return new ISOLatin1FileReader();
	}

	@Override
	public DataParser createParser() {
		return new AddressDataParser();
	}

	@Override
	public DataConverter createConverter() {
		return new AddressDataConverter();
	}

}
