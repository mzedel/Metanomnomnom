package de.metanome.algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.results.FunctionalDependency;

public class ComparableFunctionalDependency implements Comparable<ComparableFunctionalDependency> {

  Integer dependant;
  Set<Integer> determinants = new HashSet<Integer>();
  
  
  public ComparableFunctionalDependency(Set<Integer> comb, Integer ident) {
    this.determinants = comb;
    this.dependant = ident;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 7;
    result = prime * result + dependant;
//    result = prime * result + (null == determinants ? 0 : determinants.hashCode());
    for (Integer item : determinants)
      result = prime * result + item;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ComparableFunctionalDependency other = (ComparableFunctionalDependency) obj;
    if (dependant != other.dependant) return false;
    if (determinants == null && other.determinants != null) return false;
    if (dependant.equals(other.dependant)) {
      for (int id : determinants) {
        if (!other.determinants.contains(id)) return false;
      }
      for (int id : other.determinants) {
        if (!determinants.contains(id)) return false;
      }
    }
    return true;
  }
  
  @Override
  public int compareTo(ComparableFunctionalDependency otherFd) {
    ComparableFunctionalDependency arg0 = this;
    ComparableFunctionalDependency arg1 = otherFd;

    if (arg0 == arg1) return 0;
    else if (arg0.dependant.equals(arg1.dependant) &&
        arg0.determinants.equals(arg1.determinants))
      return 0;
    else if (arg0.dependant.equals(arg1.dependant)) {
      for (int id : arg0.determinants) {
        if (!arg1.determinants.contains(id)) return -1;
      }
      for (int id : arg1.determinants) {
        if (!arg0.determinants.contains(id)) return 1;
      }
    }
    return 0;
  }

  public FunctionalDependency toFunctionalDependency(String tableName, List<String> columnNames) {
    ColumnIdentifier[] combination = new ColumnIdentifier[this.determinants.size()];
    int j = 0;
    for (int i : this.determinants) {
        combination[j] = new ColumnIdentifier(tableName, columnNames.get(i));
        j++;
    }
    ColumnCombination cc = new ColumnCombination(combination);
    ColumnIdentifier ci = new ColumnIdentifier(tableName, columnNames.get(this.dependant));
    return new FunctionalDependency(cc, ci);
  }
}
