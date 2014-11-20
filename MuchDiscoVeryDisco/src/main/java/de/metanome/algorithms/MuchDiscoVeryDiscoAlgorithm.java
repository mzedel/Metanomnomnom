package de.metanome.algorithms;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;

public class MuchDiscoVeryDiscoAlgorithm {

  protected RelationalInputGenerator inputGenerator[] = null;
  protected InclusionDependencyResultReceiver resultReceiver = null;
  protected String[] tableNames = null;
  
  public void execute() throws AlgorithmExecutionException {
    
  }

  public String toString() {
    return this.getClass().getName();
  }
}
