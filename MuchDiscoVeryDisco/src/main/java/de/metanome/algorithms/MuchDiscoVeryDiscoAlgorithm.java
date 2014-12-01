package de.metanome.algorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;

public class MuchDiscoVeryDiscoAlgorithm {
	
	// if set to true, trivial inclusion dependencies (each column included in itself) will be returned
	private final static boolean RETURN_TRIVIAL = false;
	
	/*
	 * Candidate class handles combination of table / column names as well as
	 * I/O and cleaning. Methods from Files use UTF-8 by default.
	 */
	protected static class Candidate {
		
		// base directory of temporary files
		private final static String BASE_DIR = "classes/temp_files";
		
		private final String tableName;
		private final String columnName;
		private final Path path;
		
		private BufferedWriter writer;
		private BufferedReader reader;
		
		public Candidate(String tableName, String columnName) {
			this.tableName = tableName;
			this.columnName = columnName;
			
			this.path = Paths.get(Candidate.BASE_DIR, this.tableName + "_" + this.columnName);
		}
		
		@Override
		public void finalize() {
			try {
				if (this.writer != null) {
					this.writer.close();
				}
				if (this.reader != null) {
					this.reader.close();
				}
				this.deleteFile();
			} catch (IOException e) {}
		}
		
		public BufferedWriter getNewWriter() throws IOException {
			// creates a new (clean) file and returns a line writer
			if (this.writer != null) {
				try {
					this.writer.close();
				} catch (IOException e) {}
			}
			this.writer = Files.newBufferedWriter(this.path);
			return this.writer;
		}
		
		public void writeAllLines(Collection<String> lines) throws IOException {
			Files.write(this.path, lines);
		}
		
		public BufferedReader getNewReader() throws IOException {
			if (this.reader != null) {
				try {
					this.reader.close();
				} catch (IOException e) {}
			}
			this.reader = Files.newBufferedReader(this.path);
			return this.reader;
		}
		
		public List<String> readAllLines() throws IOException {
			return Files.readAllLines(this.path);
		}
		
		public void deleteFile() throws IOException {
			Files.delete(this.path);
		}
		
		public String getTableName() {
			return this.tableName;
		}
		public String getColumnName() {
			return this.columnName;
		}
		
	}
	
	protected RelationalInputGenerator inputGenerator[] = null;
	protected InclusionDependencyResultReceiver resultReceiver = null;
	protected String[] tableNames = null;

	public void execute() throws AlgorithmExecutionException {
		this.executeImpressiveAlgorithm();
	}
	
	public void executeImpressiveAlgorithm() throws AlgorithmExecutionException {
		try {
			List<Candidate> candidates = new ArrayList<Candidate>(100);
			
			/*
			 * get candidates, write data into temporary files
			 */
			for (int tableIndex = 0; tableIndex < inputGenerator.length; tableIndex++) {
				// get table name
				String tableName = this.tableNames[tableIndex];
				
				// prepare to read file (= table)
				RelationalInputGenerator generator = this.inputGenerator[tableIndex];
				
				// read header (= first row)
				RelationalInput input = generator.generateNewCopy();
				List<String> row = input.hasNext() ? input.next() : null;
				if (!input.hasNext()) continue;	// empty file
				
				// create candidate per column (does not create files yet)
				List<Candidate> tableCandidates = new ArrayList<Candidate>(row.size());
				List<BufferedWriter> writers = new ArrayList<BufferedWriter>(row.size());
				
				// write data to files
				boolean firstLine = true;
				while (input.hasNext()) {
					row = input.next();
					if (firstLine) {
						for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
							String columnName = String.valueOf(columnIndex + 1);
							Candidate candidate = new Candidate(tableName, columnName);
							tableCandidates.add(candidate);
							writers.add(candidate.getNewWriter());
						}
						firstLine = false;
					}
					for (int cellIndex = 0; cellIndex < row.size(); cellIndex++) {
						String data = row.get(cellIndex);
						if (data != null) {	// ignore null
							writers.get(cellIndex).write(data);
						}
					}
				}
				
				// close writers
				for (BufferedWriter writer : writers) {
					writer.close();
				}
				
				// add candidates of this table to the list of all candidates
				candidates.addAll(tableCandidates);
			}
			
			/*
			 * make each file (= column) sorted and distinct;
			 * assume that each column fits into memory
			 */
			for (Candidate candidate : candidates) {
				// load data into a TreeSet which is inherently sorted and distinct
				Set<String> columnSet = new TreeSet<String>(candidate.readAllLines());
				// write data to the file again
				candidate.writeAllLines(columnSet);
				// slight hint to the garbage collector
				columnSet = null;
				System.gc();
			}
			
			/*
			 * check candidates via brute force
			 */
			for (int dependentIndex = 0; dependentIndex < candidates.size(); dependentIndex++) {
				Candidate dependentCandidate = candidates.get(dependentIndex);
				candidates: for (int referencedIndex = 0; referencedIndex < candidates.size(); referencedIndex++) {
					Candidate referencedCandidate = candidates.get(referencedIndex);
					if (dependentIndex == referencedIndex) {
						// trivial case: same column
						if (MuchDiscoVeryDiscoAlgorithm.RETURN_TRIVIAL) {
							this.report(dependentCandidate.getTableName(), 
									dependentCandidate.getColumnName(), 
									referencedCandidate.getTableName(), 
									referencedCandidate.getColumnName());
						}
						// skip to next candidate
						continue;
					}
					
					BufferedReader dependentReader = dependentCandidate.getNewReader();
					BufferedReader referencedReader = referencedCandidate.getNewReader();
					
					// check if each value from the dependent column exists in the referenced column
					String dependentValue;
					String referencedValue;
					while ((dependentValue = dependentReader.readLine()) != null) {	// bad style, but works
						referencedValue = referencedReader.readLine();
						while (true) {
							if (referencedValue == null) {
								continue candidates;
							} else if (dependentValue.compareTo(referencedValue) == 0) {
								break;
							} else if (dependentValue.compareTo(referencedValue) < 0) {
								continue candidates;
							} else {
								referencedValue = referencedReader.readLine();
							}
						}
					}
					
					// if the candidate has not been skipped yet, it is included
					this.report(dependentCandidate.getTableName(), 
							dependentCandidate.getColumnName(), 
							referencedCandidate.getTableName(), 
							referencedCandidate.getColumnName());
					
					dependentReader.close();
					referencedReader.close();
				}
			}
			
			/*
			 * delete temporary files
			 */
			for (Candidate candidate : candidates) {
				candidate.deleteFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException("IOException while performing algorithm", e);
		}
	}
	
	private void report(String dependentTable, String dependentColumn, String referencedTable, String referencedColumn) 
			throws CouldNotReceiveResultException {
		this.resultReceiver.receiveResult(
			new InclusionDependency(
				new ColumnPermutation(
					new ColumnIdentifier(dependentTable, dependentColumn)),
				new ColumnPermutation(
					new ColumnIdentifier(referencedTable, referencedColumn))
			)
		);
	}

	public String toString() {
		return this.getClass().getName();
	}
}
