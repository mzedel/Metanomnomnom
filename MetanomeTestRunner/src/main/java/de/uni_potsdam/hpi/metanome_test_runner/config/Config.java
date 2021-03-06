package de.uni_potsdam.hpi.metanome_test_runner.config;

import java.io.File;

public class Config {

	public enum Algorithm {
		Metanomnomnom,
		MuchDiscoVeryDisco,
		FunctionalDerpendency
	}
	
	public enum Dataset {
		PLANETS
	}
	
	public Config.Algorithm algorithm = Config.Algorithm.FunctionalDerpendency;
	
	public String databaseName = null;
	public String[] tableNames = null;
	
	public String inputFolderPath = "data" + File.separator;
	public String inputFileEnding = ".csv";
	public char inputFileSeparator = ',';
	public char inputFileQuotechar = '\"';
	public char inputFileEscape = '\\';
	public int inputFileSkipLines = 0;
	public boolean inputFileStrictQuotes = false;
	public boolean inputFileIgnoreLeadingWhiteSpace = true;
	public boolean inputFileHasHeader = false;
	public boolean inputFileSkipDifferingLines = true; // Skip lines that differ from the dataset's schema
	
	public String measurementsFolderPath = "io" + File.separator + "measurements" + File.separator; // + "BINDER" + File.separator;
	
	public String statisticsFileName = "statistics.txt";
	public String resultFileName = "results.txt";
	
	public boolean writeResults = true;
	
	public Config() {
		this(Config.Algorithm.FunctionalDerpendency, Config.Dataset.PLANETS);
	}

	public Config(Config.Algorithm algorithm, Config.Dataset dataset) {
		this.algorithm = algorithm;
		this.setDataset(dataset);
	}

	private void setDataset(Config.Dataset dataset) {
		switch (dataset) {
			case PLANETS:
				this.databaseName = "planets";
				this.tableNames = new String[] {
				    "WDC_age", "WDC_appearances", "WDC_astrology", "WDC_astronomical", 
				    "WDC_game", "WDC_kepler", "WDC_planets", "WDC_planetz", 
				    "WDC_satellites", "WDC_science", "WDC_symbols"
				    }; // TODO: Add all tables of the planets data set here for the IND detection task!
				break;
        default:
          break;
		}
	}

	@Override
	public String toString() {
		return "Config:\r\n\t" +
			"databaseName: " + this.databaseName + "\r\n\t" +
			"tableNames: " + this.tableNames[0];
	}
}
