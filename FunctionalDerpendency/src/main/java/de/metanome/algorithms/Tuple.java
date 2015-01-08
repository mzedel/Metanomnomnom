package de.metanome.algorithms;

public class Tuple<T1, T2> {
  
  public T1 left;
  public T2 right;
  
  public Tuple(T1 left, T2 right) {
    this.left = left;
    this.right = right;
  }
  
  public T1 GetLeft() {
    return this.left;
  }
  
  public T2 GetRight() {
    return this.right;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object o) {
    Tuple<T1, T2> tuple = (Tuple<T1, T2>) o;
    return tuple.GetLeft().equals(this.GetLeft()) && 
        tuple.GetRight().equals(this.GetRight());
  }
  
}
