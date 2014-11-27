package de.metanome.algorithms;

import java.util.Set;

public class TableColumnEntry implements Comparable<TableColumnEntry> {
  
  public String TableName;
  public String ColumnName;
  public Set<String> ColumnData;

  public TableColumnEntry(String tableName, String columnName, Set<String> columnData) {
    this.TableName = tableName;
    this.ColumnName = columnName;
    this.ColumnData = columnData;
  }

  @Override
  public int compareTo(TableColumnEntry arg0) {
    // should be inverse order to sort them descending by size
    return Integer.compare(arg0.ColumnData.size(), this.ColumnData.size());
  }
  
}
