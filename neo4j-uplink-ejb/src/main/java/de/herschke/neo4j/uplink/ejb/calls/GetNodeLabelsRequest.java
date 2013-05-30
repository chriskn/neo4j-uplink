package de.herschke.neo4j.uplink.ejb.calls;

import com.sun.jersey.api.client.ClientResponse;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import java.io.IOException;
import java.io.Reader;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * creates labels for nodes.
 *
 * @author rhk
 */
public class GetNodeLabelsRequest extends AbstractNeo4jServerCall<String[]> {

    /**
     * the URI path where the server can create labels for a node
     */
    public static final String PATH_FORMAT = "node/%d/labels";
    private final int nodeId;

    public GetNodeLabelsRequest(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    protected String getPath() {
        return String.format(PATH_FORMAT, nodeId);
    }

    @Override
    protected String[] parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException {
        try {
            final JSONArray array = (JSONArray) JSONValue.parseWithException(responseContent);
            return (String[]) array.toArray(new String[array.size()]);
        } catch (IOException | ParseException ex) {
            throw new Neo4jServerException("cannot parse the response: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected String getRequestEntity() throws Neo4jServerException {
        return null;
    }

    @Override
    protected String getMethod() {
        return "GET";
    }
}
