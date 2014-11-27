package de.metanome.algorithms;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	
	protected RelationalInputGenerator inputGenerator[] = null;
	protected InclusionDependencyResultReceiver resultReceiver = null;
	protected String[] tableNames = null;

	public void execute() throws AlgorithmExecutionException {
		/*
		 * read columns of tables (simple: in main memory)
		 */
		Map<String, Map<String, Set<String>>> tableColumns 
			= new LinkedHashMap<String, Map<String, Set<String>>>();	// LinkedHashMap: preserve key order
		
		for (int tableIndex = 0; tableIndex < inputGenerator.length; tableIndex++) {
			// prepare to read next file (= table)
			RelationalInputGenerator generator = this.inputGenerator[tableIndex];
			
			// read column names (= first row)
			RelationalInput input = generator.generateNewCopy();
			List<String> row = input.hasNext() ? input.next() : null;
			if (row == null) {
				// empty file => skip
				continue;
			}
			// prepare sets for each column
			Map<String, Set<String>> columns 
				= new LinkedHashMap<String, Set<String>>();				// LinkedHashMap: preserve key order
			for (String columnName : row) {
				columns.put(columnName, new TreeSet<String>());			// TreeSet: natural ordering, distinctness
			}
			// get column key set (= column names) (LinkedHashMap: sorted)
	        String[] columnKeys = new String[columns.keySet().size()];
	        columns.keySet().toArray(columnKeys);
			// read data
			while (input.hasNext()) {
				// read next row
				row = input.next();
				// add data to columns (TreeSet: sorted / distinct)
				for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
					try {
						if (row.get(columnIndex) != null) {				// ignore null values
							columns.get(columnKeys[columnIndex]).add(row.get(columnIndex));
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						throw new AlgorithmExecutionException(
								"unexpected row size: " + (columnIndex + 1) + " > " + columnKeys.length);
					}
				}
			}
			
			// add to list of table data
			String tableName = this.tableNames[tableIndex];
			tableColumns.put(tableName, columns);
		}
		// get table key set (= table names) (LinkedHashMap: sorted)
		String[] tableKeys = new String[tableColumns.keySet().size()];
		tableColumns.keySet().toArray(tableKeys);
		
		/*
		 * search for inclusion dependencies (simple: brute force)
		 */
		for (int dependentTableIndex = 0; dependentTableIndex < tableKeys.length; dependentTableIndex++) {
			// get next table
			String dependentTable = tableKeys[dependentTableIndex];
			Map<String, Set<String>> dependentTableColumns = tableColumns.get(dependentTable);
			String[] dependentTableColumnsKeys = new String[dependentTableColumns.keySet().size()];
			dependentTableColumns.keySet().toArray(dependentTableColumnsKeys);
			for (int dependentColumnIndex = 0; dependentColumnIndex < dependentTableColumnsKeys.length; dependentColumnIndex++) {
				// get next dependent column
				String dependentColumn = dependentTableColumnsKeys[dependentColumnIndex];
				Set<String> dependentColumnSet = dependentTableColumns.get(dependentColumn);
				
				// try each candidate (= any column of any relation)
				for (int referencedTableIndex = 0; referencedTableIndex < tableKeys.length; referencedTableIndex++) {
					// get next referenced table
					String referencedTable = tableKeys[referencedTableIndex];
					Map<String, Set<String>> referencedTableColumns = tableColumns.get(referencedTable);
					String[] referencedTableColumnsKeys = new String[referencedTableColumns.keySet().size()];
					referencedTableColumns.keySet().toArray(referencedTableColumnsKeys);
					
					for (int referencedColumnIndex = 0; referencedColumnIndex < referencedTableColumnsKeys.length; referencedColumnIndex++) {
						// get next referenced column
						String referencedColumn = referencedTableColumnsKeys[referencedColumnIndex];
						Set<String> referencedColumnSet = referencedTableColumns.get(referencedColumn);
						
						// check identity of columns
						if (dependentTableIndex == referencedTableIndex
								&& dependentColumnIndex == referencedColumnIndex) {
							// same column of the same relation => trivial case, no need to check
							if (MuchDiscoVeryDiscoAlgorithm.RETURN_TRIVIAL) {
								// report trivial case
								this.report(dependentTable, dependentColumn, referencedTable, referencedColumn);
							}
							// skip to next column
							continue;
						}
						
						// try each candidate (= any column of any relation)
						if (referencedColumnSet.containsAll(dependentColumnSet)) {
							// found inclusion dependency => report
							this.report(dependentTable, dependentColumn, referencedTable, referencedColumn);
						}
					}
				}
			}
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
