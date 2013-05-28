package de.herschke.neo4j.uplink.api;

/**
 * an exception that occurs in a cypher query execution.
 *
 * @author rhk
 */
public class CypherException extends Exception {

    public CypherException() {
    }

    public CypherException(String message) {
        super(message);
    }

    public CypherException(String message, Throwable cause) {
        super(message, cause);
    }

    public CypherException(Throwable cause) {
        super(cause);
    }
}
