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
 *
 * @author rhk
 */
public class CypherResponseRowsHandler extends DelegatingContentHandler {

    private final DefaultCypherResult result;
    private int rowIndex;
    private int level = 0;
    private CypherResponseCellsHandler currentCellHandler;

    public CypherResponseRowsHandler(DefaultCypherResult result) {
        this.rowIndex = 0;
        this.result = result;
    }

    @Override
    protected ContentHandler getDelegate() {
        return this.currentCellHandler;
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        level++;
        if (this.currentCellHandler == null) {
            this.currentCellHandler = new CypherResponseCellsHandler();
            return true;
        } else {
            return super.startArray();
        }
    }

    @Override
    protected boolean handleStopAt(Token token) {
        if (token == Token.END_ARRAY && this.currentCellHandler != null) {
            result.setRowValues(this.rowIndex++, this.currentCellHandler.getResult());
            this.currentCellHandler = null;
        }
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        boolean result = super.endArray();
        level--;
        return level >= 0 && result;
    }
}
