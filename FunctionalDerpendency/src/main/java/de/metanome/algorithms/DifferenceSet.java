package de.metanome.algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.metanome.algorithm_integration.input.RelationalInput;

public class DifferenceSet extends ArrayList<String> {
  
  /**
   * 
   */
  private static final long serialVersionUID = -7995827309860186725L;

  public static List<DifferenceSet> generateDifferenceSets(RelationalInput input) {
    LinkedList<DifferenceSet> resultDifferenceSet = new LinkedList<DifferenceSet>();
    
    // Compute stripped partitions for all attributes:
    StrippedPartition strips = StrippedPartition.createStrippedPartitons(input);
    System.out.println("stripped partitions: " + strips.size());
    
    // Compute agree sets from stripped partitions:
    List<AgreeSet> tempAgreeSet = AgreeSet.calculateAgreeSets(strips);
    System.out.println("agree sets created: " + tempAgreeSet.size());

    List<String> columnNames = input.columnNames();
    
    // Complement agree sets to get difference sets : 
    for (AgreeSet set : tempAgreeSet) {
      resultDifferenceSet.add(createDifferenceSet(set, columnNames));
    }

    System.out.println("difference sets: " + resultDifferenceSet.size());
    return resultDifferenceSet;
  }
  
  private static DifferenceSet createDifferenceSet(AgreeSet set, List<String> columnNames) {
    DifferenceSet result = new DifferenceSet();
    for (String string : columnNames) {
      if(set.ColumnIds.contains(columnNames.indexOf(string)))
        result.add(string);
    }
    return result;
  }
    
}
