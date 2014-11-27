package de.metanome.algorithms;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
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
		TreeSet<TableColumnEntry> tableColumns 
			= new TreeSet<TableColumnEntry>();
		// create non-natural ordering in column collection to start by comparing probably relevant tables and 
        // discard more and more tables and thus reduce comparisons
		
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
			for (String columnName : columns.keySet()) {
			  tableColumns.add(new TableColumnEntry(tableName, columnName, columns.get(columnName)));
			}
		}
		
		for (TableColumnEntry column : tableColumns) {
		  checkOtherColumns(column, tableColumns);
		}
	}
	
	@SuppressWarnings("unused")
  private void checkOtherColumns(TableColumnEntry column, NavigableSet<TableColumnEntry> navigableSet) throws AlgorithmExecutionException {
	  if (navigableSet.isEmpty()) return;
	  TableColumnEntry currentItem = navigableSet.first();
	  boolean isTrivial = column.TableName.equals(currentItem.TableName) && column.ColumnName.equals(currentItem.ColumnName);
	  if (MuchDiscoVeryDiscoAlgorithm.RETURN_TRIVIAL && isTrivial) {
        this.report(column.TableName, column.ColumnName, currentItem.TableName, currentItem.ColumnName);
	  } else if (!isTrivial && column.ColumnData.containsAll(currentItem.ColumnData)) {
	    this.report(column.TableName, column.ColumnName, currentItem.TableName, currentItem.ColumnName);
	  }
	  if(navigableSet.size() > 1)
	    checkOtherColumns(column, navigableSet.tailSet(currentItem, false));
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
