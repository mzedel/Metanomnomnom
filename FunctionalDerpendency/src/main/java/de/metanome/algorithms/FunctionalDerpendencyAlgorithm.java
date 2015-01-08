package de.metanome.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;

public class FunctionalDerpendencyAlgorithm {

  protected FileInputGenerator inputGenerator = null;
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
    Set<ComparableFunctionalDependency> result = new LinkedHashSet<ComparableFunctionalDependency>();
    System.out.println("let's gooooo:" + diffSets.size()); 
    List<Integer> ordering = new ArrayList<Integer>();
    for (int i = 0; i < columnNames.size(); i++) {
      ordering.add(i);
    }
    for (int attr = 0; attr < columnNames.size(); attr++) { 
      Set<DifferenceSet> DA = computeSubSets(diffSets, attr);
      Set<DifferenceSet> DA2 = new LinkedHashSet<DifferenceSet>(DA);
      if (!DA.isEmpty()) {
        List<Integer> order = new ArrayList<Integer>();
        for (Integer item : ordering) {
          if (item != attr) order.add(item);
        }
        result.addAll(findCovers(attr, DA, DA2, new LinkedList<Integer>(), order));
      } 
//      else {
//        result.add(new ComparableFunctionalDependency(new HashSet<Integer>(), attr));
//      }
    }
    
    for (ComparableFunctionalDependency dep : result) {
      this.resultReceiver.receiveResult(dep.toFunctionalDependency(tableName, columnNames));
    }
  }
  
  private Set<ComparableFunctionalDependency> findCovers(Integer attribute, Set<DifferenceSet> differenceSets, 
      Set<DifferenceSet> uncoveredDifferenceSets, List<Integer> path, List<Integer> ordering) {
    Set<ComparableFunctionalDependency> result = new LinkedHashSet<ComparableFunctionalDependency>();

//    System.out.println(path);
    if (ordering.isEmpty() && !uncoveredDifferenceSets.isEmpty()) {
      return new LinkedHashSet<ComparableFunctionalDependency>(); // no FDs here 
    }
    if (uncoveredDifferenceSets.isEmpty()) {
      Set<DifferenceSet> DA = computeSubSets(differenceSets, attribute);
      int coverSize = computeCoverSize(DA, path) + 1;
      if (coverSize >= path.size()) {
        int ident = attribute;
        for (DifferenceSet set : differenceSets) {
          ArrayList<Integer> identifiers = new ArrayList<Integer>();
          for (Integer id : set) {
            identifiers.add(id);
          }
          result.add(new ComparableFunctionalDependency(new LinkedHashSet<Integer>(identifiers), ident));
        }
        System.out.println("Result   size: " + result.size());
        return result;
      } else {
        return new LinkedHashSet<ComparableFunctionalDependency>(); // wasted effort,non-minimal result
      }
    }

//  RecursiveCase : 
    for (Integer attr : ordering) {
//      if (attr == attribute) continue; 
      Set<DifferenceSet> nextSets = new HashSet<DifferenceSet>();
      for (DifferenceSet ds : uncoveredDifferenceSets) {
        if (covers(ds, attr))
          nextSets.add(ds);
      }
      System.out.println(uncoveredDifferenceSets);

      System.out.println("Next:");
      System.out.println(nextSets);
      uncoveredDifferenceSets.removeAll(nextSets); //difference sets of uncoveredDifferenceSets not covered by attr;
      List<Integer> order = new ArrayList<Integer>();
      for (Integer item : ordering) {
        if (!item.equals(attr)) order.add(item);
      }
      path.add(attr);
      result.addAll(findCovers(attribute, differenceSets, uncoveredDifferenceSets, path, order));
    }
    System.out.println("Resultsize: " + result.size());
    return result;
  }

  private Set<DifferenceSet> computeSubSets(Set<DifferenceSet> diffSets, int attr) {
    Set<DifferenceSet> result = new LinkedHashSet<DifferenceSet>();
    
    DifferenceSet newSet;
    for (DifferenceSet set : diffSets) {
      newSet = (DifferenceSet) set.clone();
      if (set.contains(attr)) {
        newSet.remove(attr);
      }
      if (!newSet.isEmpty())
        result.add(newSet);
    }
    return result;
  }
  
  protected boolean covers(DifferenceSet t, Integer attribute) {
    for (Integer attr : t) {
      if (attr.equals(attribute))
        return true;
    }
    return false;
  }

  private int computeCoverSize(Set<DifferenceSet> dA, List<Integer> path) {
    int biggestCover = 0;
    for (DifferenceSet set : dA) {
      int counter = 0;
      for (Integer column : set) {
        if (path.contains(column)) {
          counter++;
        }
      }
      if (counter > biggestCover)
        biggestCover += counter;
    }
    return biggestCover;
  }
  
}
