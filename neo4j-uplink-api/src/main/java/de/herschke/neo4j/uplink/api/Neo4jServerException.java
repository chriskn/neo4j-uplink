package de.herschke.neo4j.uplink.api;

/**
 * an exception that occurs in a Neo4j Server call
 *
 * @author rhk
 */
public class Neo4jServerException extends Exception {

    public Neo4jServerException() {
    }

    public Neo4jServerException(String message) {
        super(message);
    }

    public Neo4jServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public Neo4jServerException(Throwable cause) {
        super(cause);
    }
}
