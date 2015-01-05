package de.metanome.algorithms;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.results.FunctionalDependency;

public class ComparableFunctionalDependency implements Comparable<ComparableFunctionalDependency> {

  public FunctionalDependency fd;
  
  public ComparableFunctionalDependency(FunctionalDependency funcDep) {
    this.fd = funcDep;
  }
  
  @Override
  public int compareTo(ComparableFunctionalDependency otherFd) {
    FunctionalDependency arg0 = this.fd;
    FunctionalDependency arg1 = otherFd.fd;

    if (arg0 == arg1) return 0;
    else if (arg0.getDependant().equals(arg1.getDependant()) &&
        arg0.getDeterminant().equals(arg1.getDeterminant()))
      return 0;
    else if (arg0.getDependant().equals(arg1.getDependant())) {
      for (ColumnIdentifier id : arg0.getDeterminant().getColumnIdentifiers()) {
        if (!arg1.getDeterminant().getColumnIdentifiers().contains(id))
          return -1;
      }
      for (ColumnIdentifier id : arg1.getDeterminant().getColumnIdentifiers()) {
        if (!arg0.getDeterminant().getColumnIdentifiers().contains(id))
          return 1;
      }
    }
    return 0;
  }  
}
