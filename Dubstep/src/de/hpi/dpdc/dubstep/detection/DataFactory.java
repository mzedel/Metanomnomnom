package de.hpi.dpdc.dubstep.detection;

/**
 * Factory class for bundling all components for a particular source.
 */
public interface DataFactory {

	public DataReader createReader();
	public DataParser createParser();
	public DataConverter createConverter();
	
}
