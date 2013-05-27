package de.herschke.neo4j.uplink.ejb.responsehandling;

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
