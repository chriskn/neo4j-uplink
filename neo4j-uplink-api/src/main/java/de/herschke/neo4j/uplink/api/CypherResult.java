package de.herschke.neo4j.uplink.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * represents a result table that stores the result from a cypher query
 *
 * @author rhk
 */
public interface CypherResult extends Iterable<Map<String, Object>>, Serializable {

    /**
     * @return the count of columns in the result
     */
    int getColumnCount();

    /**
     * @param columnIndex a column index
     * @return the name of the column for the given index
     */
    String getColumnName(int columnIndex);

    /**
     * @return the names of all columns as a list
     */
    List<String> getColumnNames();

    /**
     * @param columnIndex a column index
     * @return the values in all rows for a specific column
     */
    List<Object> getColumnValues(int columnIndex);

    /**
     * @param columnName a column name
     * @return the values in all rows for a specific column
     */
    List<Object> getColumnValues(String columnName);

    /**
     * @return the count of rows in the result
     */
    int getRowCount();

    /**
     * @param rowIndex a row index
     * @return the data for a complete row as Map. The key of the Map is the
     * columnName.
     */
    Map<String, Object> getRowData(int rowIndex);

    /**
     * @param rowIndex
     * @return the data for a complete row
     */
    List<Object> getRowValues(int rowIndex);

    /**
     * @param rowIndex a row index
     * @param columnIndex a column index
     * @return the cell value at the given position
     */
    Object getValue(int rowIndex, int columnIndex);

    /**
     * @param rowIndex a row index
     * @param columnName a column name
     * @return the cell value at the given position
     */
    Object getValue(int rowIndex, String columnName);

    /**
     * @return all the values as a List of Map
     */
    List<Map<String, Object>> getAllValues();

    /**
     * @return wether or not this result has data
     */
    boolean isEmpty();
}
