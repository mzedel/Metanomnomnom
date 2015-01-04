package de.metanome.algorithms;

import java.util.ArrayList;
import java.util.List;

public class EquivalenceClass {

  public List<Tuple<Integer, Integer>> Items = new ArrayList<Tuple<Integer, Integer>>();
  
  public EquivalenceClass(Integer desc, Integer item) {
    this.Items.add(new Tuple<Integer, Integer>(desc, item));
  }
}
