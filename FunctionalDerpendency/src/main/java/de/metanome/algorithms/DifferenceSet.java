package de.metanome.algorithms;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.metanome.algorithm_integration.input.RelationalInput;

public class DifferenceSet extends LinkedHashSet<Integer> implements Comparable<DifferenceSet> {
  
  /**
   * 
   */
  private static final long serialVersionUID = -7995827309860186725L;

  public static Set<DifferenceSet> generateDifferenceSets(RelationalInput input) {
    Set<DifferenceSet> resultDifferenceSet = new LinkedHashSet<DifferenceSet>();
    
    // Compute stripped partitions for all attributes:
    StrippedPartitions strips = StrippedPartitions.createStrippedPartitons(input);
    
    // Compute agree sets from stripped partitions:
    Set<AgreeSet> tempAgreeSet = AgreeSet.calculateAgreeSets(strips);

    List<String> columnNames = input.columnNames();
    // Complement agree sets to get difference sets : 
    for (AgreeSet set : tempAgreeSet) {
      System.out.println(set.ColumnIds);
      DifferenceSet ds = createDifferenceSet(set, columnNames);
      resultDifferenceSet.add(ds);
    }
    return resultDifferenceSet;
  }
  
  private static DifferenceSet createDifferenceSet(AgreeSet set, List<String> columnNames) {
    DifferenceSet result = new DifferenceSet();
    for (String s : columnNames) {
      int i = columnNames.indexOf(s);
      if(!set.ColumnIds.contains(i))
        result.add(i);
    }
    if (result.isEmpty() && set.ColumnIds.size() == columnNames.size())
      result.addAll(set.ColumnIds);
    return result;
  }
  
  public boolean equals(Object o) {
    DifferenceSet set = (DifferenceSet) o;
    return (set.size() == this.size() && set.containsAll(this));
  }

  @Override
  public int compareTo(DifferenceSet o) {
    DifferenceSet a = this;
    DifferenceSet b = (DifferenceSet) o;
    if(a == b) return 0;
    else if(b == null) return 1;
    else if(a.size() > b.size()) return 1;
    else if(b.size() > a.size()) return -1;
    else {
      Iterator<Integer> itB = b.iterator();
      for (Integer itemA : this) {
        Integer itemB = itB.next();
        if (!itemA.equals(itemB))
         return itemA.compareTo(itemB);
      }
      return 0;
    }
  }
}
