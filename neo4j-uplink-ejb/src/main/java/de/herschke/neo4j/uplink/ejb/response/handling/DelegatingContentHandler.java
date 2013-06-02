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
public abstract class DelegatingContentHandler implements ContentHandler {

    public static enum Token {

        START_OBJECT,
        END_OBJECT,
        START_ARRAY,
        END_ARRAY,
        START_OBJECT_ENTRY,
        END_OBJECT_ENTRY,
        PRIMITIVE
    }

    protected abstract boolean handleStopAt(Token token);

    protected abstract ContentHandler getDelegate();

    @Override
    public void startJSON() throws ParseException, IOException {
        if (getDelegate() != null) {
            getDelegate().startJSON();
        }
    }

    @Override
    public void endJSON() throws ParseException, IOException {
        if (getDelegate() != null) {
            getDelegate().endJSON();
        }
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        if (getDelegate() == null) {
            return true;
        }
        if (!getDelegate().startObject()) {
            return handleStopAt(Token.START_OBJECT);
        } else {
            return true;
        }
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        if (getDelegate() == null) {
            return true;
        }
        if (!getDelegate().endObject()) {
            return handleStopAt(Token.END_OBJECT);
        } else {
            return true;
        }
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if (getDelegate() == null) {
            return true;
        }
        if (!getDelegate().startObjectEntry(key)) {
            return handleStopAt(Token.START_OBJECT_ENTRY);
        } else {
            return true;
        }
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        if (getDelegate() == null) {
            return true;
        }
        if (!getDelegate().endObjectEntry()) {
            return handleStopAt(Token.END_OBJECT_ENTRY);
        } else {
            return true;
        }
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        if (getDelegate() == null) {
            return true;
        }
        if (!getDelegate().startArray()) {
            return handleStopAt(Token.START_ARRAY);
        } else {
            return true;
        }
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        if (getDelegate() == null) {
            return true;
        }
        if (!getDelegate().endArray()) {
            return handleStopAt(Token.END_ARRAY);
        } else {
            return true;
        }
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        if (getDelegate() == null) {
            return true;
        }
        if (!getDelegate().primitive(value)) {
            return handleStopAt(Token.PRIMITIVE);
        } else {
            return true;
        }
    }
}
