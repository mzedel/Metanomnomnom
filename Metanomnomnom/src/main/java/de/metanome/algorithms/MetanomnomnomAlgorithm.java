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

import java.util.List;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;

public class MetanomnomnomAlgorithm {
	
	protected RelationalInputGenerator inputGenerator = null;
	protected UniqueColumnCombinationResultReceiver resultReceiver = null;
	
	public void execute() throws AlgorithmExecutionException {
		
		//////////////////////////////////
		// THE ALGORITHM LIVES HERE :-) //
		//////////////////////////////////
		
		// To test if the algorithm gets data
		this.print();
	}
	
	protected void print() throws InputGenerationException, InputIterationException {
		RelationalInput input = this.inputGenerator.generateNewCopy();
		
		System.out.println(input.relationName());
		
		while (input.hasNext()) {
			System.out.print("| ");
			
			List<String> record = input.next();
			for (String value : record)
				System.out.print(value + " | ");
			
			System.out.println();
		}
	}
}
