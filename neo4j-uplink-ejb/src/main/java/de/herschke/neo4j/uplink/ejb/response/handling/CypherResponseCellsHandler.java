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
package de.herschke.neo4j.uplink.ejb.response.handling;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

/**
 * this {@link ContentHandler} consumes the value of a cell inside a cypher
 * query result
 *
 * @author rhk
 */
public class CypherResponseCellsHandler extends AbstractContentHandler {

    private int level = 0;
    private final Stack valueStack = new Stack();

    public CypherResponseCellsHandler() {
        final JSONArray value = new JSONArray();
        consumeValue(value);
        valueStack.push(value);
    }

    public List getResult() {
        return Collections.unmodifiableList((List) valueStack.pop());
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        level++;
        List array = new JSONArray();
        consumeValue(array);
        valueStack.push(array);
        return true;
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        level++;
        Map object = new JSONObject();
        consumeValue(object);
        valueStack.push(object);
        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        level++;
        valueStack.push(key);
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        level--;
        trackBack();
        return level >= 0;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        level--;
        trackBack();
        return level >= 0;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        level--;
        Object value = valueStack.pop();
        Object key = valueStack.pop();
        Map parent = (Map) valueStack.peek();
        parent.put(key, value);
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        consumeValue(value);
        return true;
    }

    private void trackBack() {
        if (valueStack.size() > 1) {
            Object value = valueStack.pop();
            Object prev = valueStack.peek();
            if (prev instanceof String) {
                valueStack.push(value);
            }
        }
    }

    private void consumeValue(Object value) {
        if (valueStack.size() == 0) {
            valueStack.push(value);
        } else {
            Object prev = valueStack.peek();
            if (prev instanceof List) {
                List array = (List) prev;
                array.add(value);
            } else {
                valueStack.push(value);
            }
        }
    }
}
