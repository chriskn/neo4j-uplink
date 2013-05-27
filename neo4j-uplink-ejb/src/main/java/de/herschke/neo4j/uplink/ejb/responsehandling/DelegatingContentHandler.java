/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.herschke.neo4j.uplink.ejb.responsehandling;

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
