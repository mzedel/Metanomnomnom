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

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementFileInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingString;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;

public class FunctionalDerpendency extends FunctionalDerpendencyAlgorithm 
  implements StringParameterAlgorithm, FileInputParameterAlgorithm, FunctionalDependencyAlgorithm {

  private ArrayList<ConfigurationRequirement> conf = new ArrayList<>();

  public enum Identifier {
    INPUT_GENERATOR
  };
  
  @Override
  public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {
    conf.add(new ConfigurationRequirementFileInput(FunctionalDerpendency.Identifier.INPUT_GENERATOR.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));
//    conf.add(new ConfigurationRequirementRelationalInput("File_Input"));
    return conf;
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    super.execute();
  }

//  @Override
//  public void setFileInputConfigurationValue(String identifier, FileInputGenerator... values) throws AlgorithmConfigurationException {
//      if (Identifier.INPUT_GENERATOR.name().equals(identifier)) {
//          this.inputGenerator = values;
//
//          this.tableNames = new String[values.length];
//          for (int i = 0; i < values.length; i++) {
//              this.tableNames[i] = values[i].getInputFile().getName().split("\\.")[0];
//          }
//      } else {
//          throw new AlgorithmConfigurationException("Input generator does not match the expected identifier: " + identifier + " (given) but " + FunctionalDerpendency.Identifier.INPUT_GENERATOR.name() + " (expected)");
//      }
//  }

  @Override
  public void setResultReceiver(FunctionalDependencyResultReceiver resultReceiver) {
    super.resultReceiver = resultReceiver;
  }

  @Override
  public void setStringConfigurationValue(String identifier, String... values) {
    ConfigurationSettingString[] sdr = new ConfigurationSettingString[values.length];
    int count = 0;
    for(String value : values) {
      sdr[count] = new ConfigurationSettingString(value);
      count++;
    }
    ConfigurationRequirementString req = new ConfigurationRequirementString(identifier, values.length);
    try {
      req.setSettings(sdr);
    } catch (AlgorithmConfigurationException e) { e.printStackTrace(); }
    if ((count = conf.indexOf(req)) >= 0) {
      conf.set(count, req);
    }
  }

//  @Override
//  public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
//    if (identifier.equals("Relational_Input"))
//      super.inputGenerator = values[0];
//  }

  @Override
  public void setFileInputConfigurationValue(String identifier, FileInputGenerator... values)
      throws AlgorithmConfigurationException {
      super.inputGenerator = values[0];
  }
}
