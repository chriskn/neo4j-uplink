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
public abstract class AbstractContentHandler implements ContentHandler {

    @Override
    public void startJSON() throws ParseException, IOException {
    }

    @Override
    public void endJSON() throws ParseException, IOException {
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        return true;
    }
}
