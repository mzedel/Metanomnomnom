package de.metanome.algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;

public class AgreeSet {
  
  List<Integer> ColumnIds = new ArrayList<Integer>();
  
  public AgreeSet(List<EquivalenceClass> ecs) {
    for (EquivalenceClass ec : ecs) {
      for (Tuple<Integer, Integer> item : ec.Items) {
        this.ColumnIds.add(item.GetLeft());
      }
    }
  }
  
  public static Set<AgreeSet> calculateAgreeSets(StrippedPartitions partitions) {
    List<ComparableOpenBitSet> max = StrippedPartitions.CreateMaxSets(partitions);
    Map<Integer, EquivalenceClass> equivalenceClasses = new LinkedHashMap<Integer, EquivalenceClass>();
    for (Entry<Integer, LinkedList<ComparableOpenBitSet>> partition : partitions.entrySet()) {
      int partsIndex = 0;
      // partition might need to be sorted in ascending order,
      // based on the appearance of the first set bit in the OpenBitSet
      for (OpenBitSet parts : partition.getValue()) {
        Integer currentBit = 0;
        Integer lastBit = 0;

        while ((currentBit = parts.nextSetBit(lastBit)) != -1) {
          lastBit = currentBit;
          if (currentBit == -1) {
            break;
          }
          EquivalenceClass ec = equivalenceClasses.get(currentBit);
          if (ec == null) {
            ec = new EquivalenceClass(partition.getKey(), partsIndex);
            equivalenceClasses.put(currentBit, ec);
          } else 
            ec.Items.add(new Tuple<Integer, Integer>(partition.getKey(), partsIndex));
          lastBit++;
        }
        partsIndex++;
      }
    }
    Set<AgreeSet> agreeSets = new LinkedHashSet<AgreeSet>();
    List<EquivalenceClass> intersections = new ArrayList<EquivalenceClass>();
    for (OpenBitSet bits : max) {
      Integer currentBit = 0;
      Integer lastBit = 0;
      List<EquivalenceClass> toIntersect = new ArrayList<EquivalenceClass>();
      while ((currentBit = bits.nextSetBit(lastBit)) != -1) {
        toIntersect.add(equivalenceClasses.get(currentBit));
        lastBit = currentBit;
        lastBit++;
      }
      if (intersections.isEmpty())
        intersections.addAll(toIntersect);
      intersections.retainAll(toIntersect);
      if (!intersections.isEmpty())
        agreeSets.add(new AgreeSet(intersections));
    }
    return agreeSets;
  }
  
}
