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
package de.herschke.neo4j.uplink.ejb.response.conversion;

import de.herschke.neo4j.uplink.api.CypherResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rhk
 */
public class DummyCypherResult implements CypherResult {
    private final List<String> columns = new ArrayList();
    private final List<Map<String, Object>> data = new ArrayList();

    public DummyCypherResult(String column, String... columns) {
        this.columns.add(column);
        this.columns.addAll(Arrays.asList(columns));
    }

    public void setColumnValues(String columnName, Object... values) {
        for (int i = 0; i < values.length; i++) {
            setValueAt(i, columnName, values[i]);
        }
    }

    void setValueAt(int rowIndex, String columnName, Object value) {
        // ensure capacity
        for (int i = data.size(); i <= rowIndex; i++) {
            data.add(new HashMap<String, Object>());
        }
        // get the row for the rowIndex
        Map<String, Object> row = this.data.get(rowIndex);
        // set the field...
        row.put(columnName, value);
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns.get(columnIndex);
    }

    @Override
    public List<String> getColumnNames() {
        return columns;
    }

    @Override
    public List<Object> getColumnValues(int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Object> getColumnValues(String columnName) {
        List<Object> result = new ArrayList<>();
        for (Map<String, Object> row : data) {
            result.add(row.get(columnName));
        }
        return result;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Map<String, Object> getRowData(int rowIndex) {
        return data.get(rowIndex);
    }

    @Override
    public List<Object> getRowValues(int rowIndex) {
        return new ArrayList(getRowData(rowIndex).values());
    }

    @Override
    public Object getValue(int rowIndex, int columnIndex) {
        return getValue(rowIndex, getColumnName(columnIndex));
    }

    @Override
    public Object getValue(int rowIndex, String columnName) {
        return data.get(rowIndex).get(columnName);
    }

    @Override
    public List<Map<String, Object>> getAllValues() {
        return data;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public String getCypher() {
        return "";
    }

    @Override
    public Map<String, Object> getQueryParameter() {
        return Collections.emptyMap();
    }

    @Override
    public Iterator<Map<String, Object>> iterator() {
        return data.iterator();
    }
}
