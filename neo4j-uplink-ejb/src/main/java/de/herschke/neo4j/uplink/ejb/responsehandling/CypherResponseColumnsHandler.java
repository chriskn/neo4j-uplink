package de.herschke.neo4j.uplink.ejb.responsehandling;

import java.io.IOException;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rhk
 */
public class CypherResponseColumnsHandler extends AbstractContentHandler {

    private final DefaultCypherResult result;

    public CypherResponseColumnsHandler(DefaultCypherResult result) {
        this.result = result;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        return false;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        if (value != null && value instanceof String) {
            result.addColumn((String) value);
            return true;
        } else {
            throw new IOException("while fetching columns, only strings are allowed!");
        }
    }
}
