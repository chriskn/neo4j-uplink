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
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

/**
 * a {@link ContentHandler} that is used to parse the response of a cypher
 * query.
 * <p>
 * Assuming, the following cypher query was sent:
 * <p>
 * <code>
 *   start x  = node(335) match x -[r]-> n return type(r), n.name?, n.age?
 * </code>
 * <p>
 * So here's an example of a standard cypher response:
 * <p>
 * <pre>
 * {
 *   "columns" :
 *     [
 *       "type(r)",
 *       "n.name?",
 *       "n.age?"
 *     ],
 *   "data" :
 *     [
 *       [
 *         "know",
 *         "him",
 *         25
 *       ],
 *       [
 *         "know",
 *         "you",
 *         null
 *       ]
 *     ]
 * }
 * </pre>
 * <p>
 * So this ContentHandler ignores all other content beside "columns" and "data"
 * object entries and extracts the columns and the data as list.
 *
 * @author rhk
 */
public class CypherResponseHandler extends DelegatingContentHandler {

    /**
     * the data of the response.
     */
    private DefaultCypherResult result;
    private ContentHandler currentContentHandler = null;
    private boolean hasFetchedColumns = false;
    private boolean hasFetchedData = false;
    private boolean fetchingData = false;

    public DefaultCypherResult getResult() {
        return this.result;
    }

    @Override
    public void startJSON() throws ParseException, IOException {
        this.result = new DefaultCypherResult();
        this.currentContentHandler = null;
        this.hasFetchedColumns = false;
        this.hasFetchedData = false;
        this.fetchingData = false;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if (this.currentContentHandler == null) {
            switch (key) {
                case "columns":
                    this.currentContentHandler = new CypherResponseColumnsHandler(result);
                    return true;
                case "data":
                    this.currentContentHandler = new CypherResponseRowsHandler(result);
                    return true;
                default:
                    return true;
            }
        } else {
            return super.startObjectEntry(key);
        }

    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        if (this.currentContentHandler != null && this.currentContentHandler instanceof CypherResponseRowsHandler && !fetchingData) {
            // start array for data rows...
            fetchingData = true;
            return true;
        }
        return super.startArray();
    }

    @Override
    protected boolean handleStopAt(Token token) {
        if (this.currentContentHandler != null) {
            if (this.currentContentHandler instanceof CypherResponseColumnsHandler) {
                this.hasFetchedColumns = true;
                this.currentContentHandler = null;
            } else if (this.currentContentHandler instanceof CypherResponseRowsHandler) {
                this.fetchingData = false;
                this.hasFetchedData = true;
                this.currentContentHandler = null;
            }
        }
        return !(hasFetchedColumns && hasFetchedData);
    }

    @Override
    protected ContentHandler getDelegate() {
        return this.currentContentHandler;
    }
}
