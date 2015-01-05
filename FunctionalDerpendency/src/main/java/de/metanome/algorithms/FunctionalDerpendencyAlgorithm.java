package de.metanome.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

    Set<DifferenceSet> diffSets = DifferenceSet.generateDifferenceSets(input);
    Set<ComparableFunctionalDependency> result = new TreeSet<ComparableFunctionalDependency>();
    
    List<Integer> ordering = new ArrayList<Integer>();
    for (int i = 0; i < columnNames.size(); i++) {
      ordering.add(i);
    }
    for (int attr = 0; attr < columnNames.size(); attr++) { 
      Set<DifferenceSet> DA = computeSubSets(diffSets, attr);
      Set<DifferenceSet> DA2 = new TreeSet<DifferenceSet>(DA);
      if (!DA.isEmpty()) {
        List<Integer> order = new ArrayList<Integer>();
        for (Integer item : ordering) {
          if (item != attr) order.add(item);
        }
        result = findCovers(attr, DA, DA2, new LinkedList<Integer>(), order);
      }
    }
    
    for (ComparableFunctionalDependency dep : result) {
      this.resultReceiver.receiveResult(dep.fd);
    }
  }
  
  private Set<ComparableFunctionalDependency> findCovers(Integer attribute, Set<DifferenceSet> differenceSets, 
      Set<DifferenceSet> uncoveredDifferenceSets, List<Integer> path, List<Integer> ordering) {
    Set<ComparableFunctionalDependency> result = new TreeSet<ComparableFunctionalDependency>();
    if (ordering.isEmpty() && !uncoveredDifferenceSets.isEmpty()) {
      return new TreeSet<ComparableFunctionalDependency>(); // no FDs here 
    }
    if (uncoveredDifferenceSets.isEmpty()) {
      Set<DifferenceSet> DA = computeSubSets(differenceSets, attribute);
      int coverSize = computeCoverSize(DA, path);
      if (coverSize >= path.size()) {
        ColumnIdentifier ident = new ColumnIdentifier(tableName, columnNames.get(attribute));
        Set<ComparableFunctionalDependency> list = new TreeSet<ComparableFunctionalDependency>();
        for (DifferenceSet set : differenceSets) {
          ColumnIdentifier[] identifiers = new ColumnIdentifier[set.size()];
          Iterator<String> it = set.iterator();
          for (int i = 0; i < set.size(); i++) {
            String id = it.next();
            identifiers[i] = new ColumnIdentifier(tableName, id);
          }
          ColumnCombination comb = new ColumnCombination(identifiers);
          list.add(new ComparableFunctionalDependency(new FunctionalDependency(comb, ident)));
        }
        return list;
      } else {
        return new TreeSet<ComparableFunctionalDependency>(); // wasted effort,non-minimal result
      }
    }

//  RecursiveCase : 
    for (Integer attr : ordering) {
//      if (attr == attribute) continue; 
      Set<DifferenceSet> nextSets = new TreeSet<DifferenceSet>();
      for (DifferenceSet ds : uncoveredDifferenceSets) {
        if (covers(ds, attr))
          nextSets.add(ds);
      }
      uncoveredDifferenceSets.removeAll(nextSets); //difference sets of uncoveredDifferenceSets not covered by attr;
      nextSets = uncoveredDifferenceSets;
      List<Integer> order = new ArrayList<Integer>();
      for (Integer item : ordering) {
        if (!item.equals(attr)) order.add(item);
      }
      path.add(attr);
      result.addAll(findCovers(attribute, differenceSets, nextSets, path, order));
    }
    return result;
  }

  private Set<DifferenceSet> computeSubSets(Set<DifferenceSet> diffSets, int attr) {
    Set<DifferenceSet> result = new TreeSet<DifferenceSet>();
    
    String string = columnNames.get(attr);
    DifferenceSet newSet;
    for (DifferenceSet set : diffSets) {
      newSet = (DifferenceSet) set.clone();
      if (set.contains(string)) {
        newSet.remove(string);
      }
      result.add(newSet);
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

  private int computeCoverSize(Set<DifferenceSet> dA, List<Integer> path) {
    int biggestCover = 0;
    for (DifferenceSet set : dA) {
      int counter = 0;
      for (String column : set) {
        int columnIndex = columnNames.indexOf(column);
        if (path.contains(columnIndex)) {
          counter++;
        }
      }
      if (counter > biggestCover)
        biggestCover = counter;
    }
    return biggestCover;
  }
  
}
