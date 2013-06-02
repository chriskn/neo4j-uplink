/*
 * This file is part of the Uplink Framework for Neo4j Server Connection.
 *
 * Copyright (c) by:
 *
 * Robert Herschke
 * Hauptstrasse 30
 * 65760 Eschborn
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
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
