/*
 * Copyright 2014 by the Metanome project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.metanome.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;

public class MetanomnomnomAlgorithm {
	
	protected RelationalInputGenerator inputGenerator = null;
	protected UniqueColumnCombinationResultReceiver resultReceiver = null;
	
	public void execute() throws AlgorithmExecutionException {
		/*
		 * Naive Algorithm
		 * - read column names
		 * - read all data rows, build complete position list index
		 * - check column combinations until all UCCs are found
		 */

		// get input source
		RelationalInput input = inputGenerator.generateNewCopy();
		if (!input.hasNext()) {
			// empty input => abort
			return;
		}
		
		// read table and column names
		String tableName = input.relationName();
		List<String> columnNames = input.next();
		// assign numbers to columns, store their names accordingly
		Map<Integer, String> columns = new TreeMap<Integer, String>();
		for (int i = 0; i < columnNames.size(); i++) {
			columns.put(i, columnNames.get(i));
		}
		printList("Columns", columnNames);
		
		// create Position List Index
		PLIBuilder pliBuilder = new PLIBuilder(input);
		List<PositionListIndex> pliList = pliBuilder.getPLIList();
		
		// check column combinations
		ColumnCombinationBitset topCC = new ColumnCombinationBitset(new ArrayList<Integer>(columns.keySet()));
		List<ColumnCombinationBitset> allCCs = topCC.getAllSubsets();
		for (int i = allCCs.size() - 2; i >= 0; i--) {	// go from bottom to top, skip empty column set
			// get columns
			List<Integer> columnList = allCCs.get(i).getSetBits();
			printList("Checking CC", columnList);
			// create intersection of the PLIs
			PositionListIndex pli = null;
			for (Integer columnIndex : columnList) {
				pli = pli == null ? pliList.get(columnIndex) : pli.intersect(pliList.get(columnIndex));
			}
			// check uniqueness
			if (pli.isUnique()) {
				// report UCC
				System.out.println("\tunique!");
				List<ColumnIdentifier> identifiers = new ArrayList<ColumnIdentifier>(columnList.size());
				for (Integer columnIndex : columnList) {
					identifiers.add(new ColumnIdentifier(tableName, columns.get(columnIndex)));
				}
				this.resultReceiver.receiveResult(new UniqueColumnCombination(identifiers.toArray(new ColumnIdentifier[]{})));
			}
		}
	}
	
	private void printList(String comment, List<?> list) {
		System.out.println(comment + ": " + java.util.Arrays.toString(list.toArray()));
	}
	
	public String toString() {
		return this.getClass().getName();
	}
	
}
