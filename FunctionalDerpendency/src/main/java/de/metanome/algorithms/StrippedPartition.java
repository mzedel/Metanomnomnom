package de.metanome.algorithms;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;

public class StrippedPartition extends HashMap<Integer, LinkedList<OpenBitSet>> {
  
  /**
   * 
   */
  private static final long serialVersionUID = -533192034832365886L;
  
  public static String NULL = "NULL#" + System.currentTimeMillis();
//  public static String NULL = "NULL";
  
  private void put(Integer key, OpenBitSet object) {
    LinkedList<OpenBitSet> item = this.get(key);
    if(item == null)
      item = new LinkedList<OpenBitSet>();

    System.out.println("bitset to add: " + object);
    item.add(object);
    this.put(key, item);
  }
  
  public static StrippedPartition createStrippedPartitons (RelationalInput input) {
    Map<Integer, HashMap<String, OpenBitSet>> partitions = new HashMap<Integer, HashMap<String, OpenBitSet>>();
        
    for (int column = 0; column < input.numberOfColumns(); column++) {
      partitions.put(column, new HashMap<String, OpenBitSet>());
    }
    System.out.println("partitions: " + partitions.size());    
    List<String> line;
    int lineCounter = 0;
    int columnCounter;
    try {
      while ((line = input.next()) != null) {
//        System.out.println("input line: " + line);
        columnCounter = 0;
        for (String column : line) {
          if (null == column)
            column = StrippedPartition.NULL;
          if(partitions.get(columnCounter).get(column) == null)
            partitions.get(columnCounter).put(column, new OpenBitSet());
          partitions.get(columnCounter).get(column).set(lineCounter);
          columnCounter++;
        }
        lineCounter++;
      }
    } catch (InputIterationException e) {
      // empty file or EoF reached 
    }
    StrippedPartition strips = optimizePartitions(partitions);
    partitions.clear();
    return strips;
  }

  private static StrippedPartition optimizePartitions(Map<Integer, HashMap<String, OpenBitSet>> partitions) {
    StrippedPartition strippedPartitions = new StrippedPartition();
    for(Entry<Integer, HashMap<String, OpenBitSet>> column : partitions.entrySet()) {
      for(OpenBitSet object : column.getValue().values()) {
        if (object.cardinality() > 1) {
          strippedPartitions.put(column.getKey(), object);
        }
      }
      column.getValue().clear();
    }
    partitions.clear();
    return strippedPartitions;
  }

  public static List<OpenBitSet> CreateMaxSets(StrippedPartition partitions) {
    
    class CustomBitSetComparator implements Comparator<OpenBitSet>{
      @Override
      public int compare(OpenBitSet a, OpenBitSet b) {
        if(a == b) return 0;
        else if(a == null) return -1;
        else if(b == null) return 1;
        else if(a.equals(b)) return 0;
        else if(a.length() > b.length()) return 1;
        else if(b.length() > a.length()) return -1;
        else {
          for(int i = 0; i < a.length(); i++) {
            if(a.get(i) != b.get(i)) {
              if(a.get(i)) return 1;
              else return -1;
            }
          }
          return 0;
        }
      }
    }
    
    Set<OpenBitSet> maxSets = new TreeSet<OpenBitSet>(new CustomBitSetComparator());
    for (LinkedList<OpenBitSet> partition : partitions.values()) {
      for (LinkedList<OpenBitSet> partition2 : partitions.values()) {
        if (partition == partition2) continue;
        for (OpenBitSet partitionStrip : partition) {
          for (OpenBitSet partitionStrip2 : partition2) {
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
    return new LinkedList<OpenBitSet>(maxSets);
  }
  
  public static Boolean Covers(OpenBitSet lhs, OpenBitSet rhs)
  {
      OpenBitSet left = lhs.clone();
      OpenBitSet right = rhs.clone();
      right.and(left);
      return right.equals(rhs);
  }

}
