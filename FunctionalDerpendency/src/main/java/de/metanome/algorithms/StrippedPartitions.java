package de.metanome.algorithms;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;

public class StrippedPartitions extends LinkedHashMap<Integer, LinkedList<ComparableOpenBitSet>> {
  
  /**
   * 
   */
  private static final long serialVersionUID = -533192034832365886L;
  
  public static String NULL = "NULL#" + System.currentTimeMillis();
//  public static String NULL = "NULL";
  
  public int partitionCardinalitySum = 0;
  
  
  private void put(Integer key, ComparableOpenBitSet object) {
    LinkedList<ComparableOpenBitSet> item = this.get(key);
    if(item == null)
      item = new LinkedList<ComparableOpenBitSet>();
    item.add(object);
    this.put(key, item);
  }
  
  public static StrippedPartitions createStrippedPartitons (RelationalInput input) {
    Map<Integer, Tuple<PartitionMap, Integer>> partitions = new LinkedHashMap<Integer, Tuple<PartitionMap, Integer>>();
        
    for (int column = 0; column < input.numberOfColumns(); column++) {
      partitions.put(column, new Tuple<PartitionMap, Integer>(new PartitionMap(), 0));
    }
    List<String> line;
    int lineCounter = 0;
    int columnCounter;
    try {
      while ((line = input.next()) != null) {
        columnCounter = 0;
        for (String column : line) {
          if (null == column)
            column = StrippedPartitions.NULL;
          if(partitions.get(columnCounter).left.get(column) == null)
            partitions.get(columnCounter).left.put(column, new ComparableOpenBitSet());
          partitions.get(columnCounter).left.get(column).set(lineCounter);
          partitions.get(columnCounter).right++;
          columnCounter++;
        }
        lineCounter++;
      }
    } catch (InputIterationException e) {
      // empty file or EoF reached 
    }
    StrippedPartitions strips = optimizePartitions(partitions);
    partitions.clear();
    return strips;
  }

  private static StrippedPartitions optimizePartitions(Map<Integer, Tuple<PartitionMap, Integer>> partitions) {
    StrippedPartitions strippedPartitions = new StrippedPartitions();
    for(Entry<Integer, Tuple<PartitionMap, Integer>> column : partitions.entrySet()) {
      int partitionCount = column.getValue().left.values().size();
      int partitionCardinalitySum = column.getValue().right;
      for(ComparableOpenBitSet object : column.getValue().left.values()) {
        if ((object.cardinality() > 1) || (partitionCount == partitionCardinalitySum)) {
          strippedPartitions.put(column.getKey(), object);
        }
      }
      column.getValue().left.clear();
    }
    partitions.clear();
    return strippedPartitions;
  }

  public static List<ComparableOpenBitSet> CreateMaxSets(StrippedPartitions partitions) {
    
    Set<ComparableOpenBitSet> maxSets = new LinkedHashSet<ComparableOpenBitSet>();
    for (LinkedList<ComparableOpenBitSet> partition : partitions.values()) {
      for (LinkedList<ComparableOpenBitSet> partition2 : partitions.values()) {
        if (partition == partition2) continue;
        for (ComparableOpenBitSet partitionStrip : partition) {
          for (ComparableOpenBitSet partitionStrip2 : partition2) {
            if (Covers(partitionStrip, partitionStrip2) && partitionStrip2.cardinality() > partitionStrip.cardinality()) {
              maxSets.remove(partitionStrip);
              maxSets.add(partitionStrip2);
            } else {
              maxSets.add(partitionStrip);
            }
          }
        }
      }
    }
    return new LinkedList<ComparableOpenBitSet>(maxSets);
  }
  
  public static Boolean Covers(ComparableOpenBitSet lhs, ComparableOpenBitSet rhs)
  {
      ComparableOpenBitSet left = (ComparableOpenBitSet) lhs.clone();
      ComparableOpenBitSet right = (ComparableOpenBitSet) rhs.clone();
      right.and(left);
      return right.equals(rhs);
  }

}
