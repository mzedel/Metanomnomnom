package de.metanome.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;

public class FunctionalDerpendencyAlgorithm {

  protected RelationalInputGenerator inputGenerator = null;
  protected FunctionalDependencyResultReceiver resultReceiver = null;
  protected String[] tableNames = null;
  protected List<String> columnNames = null;
  protected String tableName = "";
  
  public void execute() throws AlgorithmExecutionException {
    /*
     * FastFDs
     * - read column names
     */

    // get input source
    RelationalInput input = inputGenerator.generateNewCopy();
    if (!input.hasNext()) {
        // empty input => abort
        return;
    }
    
    columnNames = input.columnNames();
    tableName = input.relationName();

    List<DifferenceSet> diffSets = DifferenceSet.generateDifferenceSets(input);
    List<FunctionalDependency> result = new LinkedList<FunctionalDependency>();
    
    List<Integer> ordering = new ArrayList<Integer>();
    for (int i = 0; i < columnNames.size(); i++) {
      ordering.add(i);
    }
    for (int attr = 0; attr < columnNames.size(); attr++) { 
      List<DifferenceSet> DA = computeSubSets(diffSets, attr);
      if (!DA.isEmpty()) {
        List<Integer> order = new ArrayList<Integer>();
        Collections.copy(order, ordering);
        order.remove(attr);
        result = findCovers(attr, DA, DA, new TreeSet<Integer>(), order);
      }
    }
    
    for (FunctionalDependency dep : result) {
      this.resultReceiver.receiveResult(dep);
    }
  }
  
  private List<FunctionalDependency> findCovers(Integer attribute, List<DifferenceSet> differenceSets, 
      List<DifferenceSet> uncoveredDifferenceSets, Set<Integer> path, List<Integer> ordering) {
    if (ordering.isEmpty() && !uncoveredDifferenceSets.isEmpty()) {
      return new LinkedList<FunctionalDependency>(); // no FDs here 
    }
    if (uncoveredDifferenceSets.isEmpty()) {
      List<DifferenceSet> DA = computeSubSets(differenceSets, attribute);
      if (computeCoverSize(DA, path) >= path.size()) {
        final ColumnIdentifier ident = new ColumnIdentifier(tableName, columnNames.get(attribute));
        LinkedList<FunctionalDependency> list = new LinkedList<FunctionalDependency>();
        for (DifferenceSet set : differenceSets) {
          List<ColumnIdentifier> identifiers = new ArrayList<ColumnIdentifier>();
          for (String id : set) {
            identifiers.add(new ColumnIdentifier(tableName, id));
          }
          ColumnCombination comb = new ColumnCombination((ColumnIdentifier[]) identifiers.toArray());
          list.add(new FunctionalDependency(comb, ident));
        }
        return list;
      } else {
        return new LinkedList<FunctionalDependency>(); // wasted effort,non-minimal result
      }
    }
//  RecursiveCase : 
    for (Integer attr = 0; attr < ordering.size(); attr++) {
      final int attrib = attr;
      uncoveredDifferenceSets.removeIf(new Predicate<DifferenceSet>() {
        @Override
        public boolean test(DifferenceSet t) {
          return covers(t, attrib);
        }
      }); //difference sets of uncoveredDifferenceSets not covered by attr;
      List<DifferenceSet> nextSets = uncoveredDifferenceSets;
      List<Integer> order = new ArrayList<Integer>();
      Collections.copy(order, ordering);
      order.remove(attr);
      path.add(attr);
      return findCovers(attribute, differenceSets, nextSets, path, order);
    }
    return new LinkedList<FunctionalDependency>();
  }

  private List<DifferenceSet> computeSubSets(List<DifferenceSet> diffSets, int attr) {
    List<DifferenceSet> result = new LinkedList<DifferenceSet>();
    String string = columnNames.get(attr);
    for (DifferenceSet set : diffSets) {
      if (set.contains(string)) {
        set.remove(string);
      }
      result.add(set);
    }
    return result;
  }
  
  protected boolean covers(DifferenceSet t, int attrib) {
    String attribute = columnNames.get(attrib);
    for (String attr : t) {
      if (attr.equals(attribute))
        return true;
    }
    return false;
  }

  private int computeCoverSize(List<DifferenceSet> dA, Set<Integer> path) {
    int counter = 0;
    for (DifferenceSet set : dA) {
      for (String column : set) {
        int columnIndex = columnNames.indexOf(column);
        if (path.contains(columnIndex)) {
          counter++;
        }
      }
    }
    return counter;
  }
  
}
