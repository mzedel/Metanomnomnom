package de.hpi.dpdc.dubstep.detection.address;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import de.hpi.dpdc.dubstep.detection.DataReader;

/**
 * Specific reader to read data from a text file encoded via ISO Latin 1.
 */
public class ISOLatin1FileReader implements DataReader {

	@Override
	public List<String> read(String source) throws IOException {
		return Files.readAllLines(new File(source).toPath(), StandardCharsets.ISO_8859_1);
	}

}
