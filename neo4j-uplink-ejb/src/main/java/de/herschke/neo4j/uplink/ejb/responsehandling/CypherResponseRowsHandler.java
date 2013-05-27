package de.herschke.neo4j.uplink.ejb.responsehandling;

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
