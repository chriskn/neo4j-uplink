package de.herschke.neo4j.uplink.ejb.calls;

import de.herschke.neo4j.uplink.api.Neo4jServerException;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rhk
 */
public class Neo4jServerResponseException extends Neo4jServerException {

    private final String type;
    private final String message;
    private final List<String> stacktrace;

    private Neo4jServerResponseException(String type, String message, List<String> stacktrace) {
        super(String.format("%s: %s", type, message));
        this.type = type;
        this.message = message;
        this.stacktrace = stacktrace;
    }

    public String getServerMessage() {
        return this.message;
    }

    public String getType() {
        return this.type;
    }

    public List<String> getServerStacktrace() {
        return this.stacktrace;
    }

    public static Neo4jServerResponseException fromServerResponse(Reader response) {
        try {
            JSONObject result = (JSONObject) JSONValue.parseWithException(response);
            return new Neo4jServerResponseException((String) result.get("exception"), (String) result.get("message"), (List<String>) result.get("stacktrace"));
        } catch (IOException | ParseException ex) {
            throw new IllegalArgumentException("cannot parse the Server Response: " + ex.getMessage(), ex);
        }
    }
}
