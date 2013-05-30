package de.herschke.neo4j.uplink.ejb.calls;

import com.sun.jersey.api.client.ClientResponse;
import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.ejb.responsehandling.CypherResponseHandler;
import de.herschke.neo4j.uplink.ejb.utils.JSONHelper;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * executes the capher.
 *
 * @author rhk
 */
public class ExecuteCypherRequest extends AbstractNeo4jServerCall<CypherResult> {

    /**
     * the URI path where the server can execute cypher queries
     */
    public static final String PATH = "cypher";
    private final String query;
    private final Map<String, Object> params = new HashMap<>();

    public ExecuteCypherRequest(String query) {
        this.query = query;
    }

    public ExecuteCypherRequest(String query, Map<String, Object> params) {
        this.query = query;
        this.params.putAll(params);
    }

    public ExecuteCypherRequest withParameter(String name, Object value) {
        this.params.put(name, value);
        return this;
    }

    @Override
    protected String getRequestEntity() throws Neo4jServerException {
        JSONObject object = new JSONObject();
        object.put("query", query);
        object.put("params", JSONHelper.toDeepJSONObject(params));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(object.toJSONString());
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        return object.toJSONString();
    }

    @Override
    protected String getPath() {
        return PATH;
    }

    @Override
    protected CypherResult parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException {
        switch (status) {
            case OK:
                return parseCypherResponse(responseContent);
            default:
                throw new IllegalStateException(String.format("%s must not call parseResponse for status: %s", getClass().getSuperclass().getSimpleName(), status.name()));
        }
    }

    private CypherResult parseCypherResponse(Reader response) throws Neo4jServerException {
        try {
            final CypherResponseHandler handler = new CypherResponseHandler();
            JSONParser parser = new JSONParser();
            parser.parse(new LoggingReader(response), handler);
            return handler.getResult();
        } catch (IOException | ParseException ex) {
            throw new Neo4jServerException(String.format("cannot parse the cypher response: %s", ex.getMessage()), ex);
        }
    }
}
