package de.metanome.algorithms;

import org.apache.lucene.util.OpenBitSet;

public class ComparableOpenBitSet extends OpenBitSet implements Comparable<OpenBitSet> {

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for(Long l : this.bits) {
      builder.append(Long.toBinaryString(l));
    };
    return builder.toString();
  }
  
  @Override
  public int compareTo(OpenBitSet b) {
    OpenBitSet a = this;
    if(a == b) return 0;
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