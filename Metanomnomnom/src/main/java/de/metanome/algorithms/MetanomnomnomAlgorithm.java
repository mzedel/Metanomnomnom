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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		 * Simple Algorithm
		 * - read column names
		 * - read all data rows, build complete position list index
		 * - build complete tree of all column combinations
		 * - check column combinations until all minimal UCCs are found;
		 *   use found UCCs for pruning (if a CC contains a UCC, skip it)
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
		
		// create Position List Index, null != null
		PLIBuilder pliBuilder = new PLIBuilder(input, false);
		List<PositionListIndex> pliList = pliBuilder.getPLIList();
		
		/*
		 * check column combinations
		 */
		
		// prepare list of UCCs
		List<List<Integer>> uccs = new ArrayList<List<Integer>>();
		
		// get set of all column sets
		ColumnCombinationBitset topCC = new ColumnCombinationBitset(new ArrayList<Integer>(columns.keySet()));
		int topCCSize = columns.size();
		List<ColumnCombinationBitset> allCCs = topCC.getAllSubsets();	// lists CCs from top to bottom
		Collections.reverse(allCCs);									// list from bottom to top
		allCCs.remove(new ColumnCombinationBitset());					// remove empty set
		
		// check column sets from bottom to top (skip empty column set)
		outer: while (allCCs.size() > 0) {
			// get columns
			List<Integer> columnList = allCCs.get(0).getSetBits();
			
			// check if CC is a superset of any known UCC (need only minimal UCCs)
			for (List<Integer> ucc : uccs) {
				if (columnList.containsAll(ucc)) {
					// skip to next CC
					allCCs.remove(allCCs.get(0));
					continue outer;
				}
			}
			
			// create intersection of the PLIs
			PositionListIndex pli = null;
			for (Integer columnIndex : columnList) {
				pli = pli == null ? pliList.get(columnIndex) : pli.intersect(pliList.get(columnIndex));
			}
			
			// check uniqueness
			if (pli.isUnique()) {
				
				// remove candidates
				int superSetSize = columnList.size();
				ColumnCombinationBitset uccBitset = new ColumnCombinationBitset(columnList);
				Set<ColumnCombinationBitset> sets = new HashSet<ColumnCombinationBitset>();
				Set<ColumnCombinationBitset> temp = new HashSet<ColumnCombinationBitset>();
				sets.add(uccBitset);
				do {
					superSetSize++;
					for (ColumnCombinationBitset bitset : sets) {
						List<ColumnCombinationBitset> superSets = bitset.getDirectSupersets(topCC);
						temp.addAll(superSets);
						allCCs.removeAll(superSets);
					}
					sets = temp;
					temp = new HashSet<ColumnCombinationBitset>();
				} while (superSetSize <= topCCSize);
				
				// remember UCC for pruning
				uccs.add(columnList);
				
				// report UCC
				List<ColumnIdentifier> identifiers = new ArrayList<ColumnIdentifier>(columnList.size());
				for (Integer columnIndex : columnList) {
					identifiers.add(new ColumnIdentifier(tableName, columns.get(columnIndex)));
				}
				this.resultReceiver.receiveResult(new UniqueColumnCombination(identifiers.toArray(new ColumnIdentifier[]{})));
			}
			allCCs.remove(0);
		}
	}
	
	public String toString() {
		return this.getClass().getName();
	}
	
}
