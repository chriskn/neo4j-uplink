package de.herschke.neo4j.uplink.ejb.responsehandling;

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
