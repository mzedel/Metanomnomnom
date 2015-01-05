package de.metanome.algorithms;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.metanome.algorithm_integration.input.RelationalInput;

public class DifferenceSet extends TreeSet<String> implements Comparable<DifferenceSet> {
  
  /**
   * 
   */
  private static final long serialVersionUID = -7995827309860186725L;

  public static Set<DifferenceSet> generateDifferenceSets(RelationalInput input) {
    Set<DifferenceSet> resultDifferenceSet = new TreeSet<DifferenceSet>();
    
    // Compute stripped partitions for all attributes:
    StrippedPartition strips = StrippedPartition.createStrippedPartitons(input);
    
    // Compute agree sets from stripped partitions:
    List<AgreeSet> tempAgreeSet = AgreeSet.calculateAgreeSets(strips);

    List<String> columnNames = input.columnNames();
    // Complement agree sets to get difference sets : 
    for (AgreeSet set : tempAgreeSet) {
      resultDifferenceSet.add(createDifferenceSet(set, columnNames));
    }
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
      Iterator<String> itB = b.iterator();
      for (String itemA : this) {
        String itemB = itB.next();
        if (!itemA.equals(itemB))
         return itemA.compareTo(itemB);
      }
      return 0;
    }
  }
}
