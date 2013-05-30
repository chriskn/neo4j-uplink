package de.herschke.neo4j.uplink.ejb.calls;

import com.sun.jersey.api.client.ClientResponse;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.api.Node;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * creates labels for nodes.
 *
 * @author rhk
 */
public class GetNodesWithLabelRequest extends AbstractNeo4jServerCall<Node[]> {

    /**
     * the URI path where the server can get the nodes for the label
     */
    public static final String PATH_FORMAT = "label/%s/nodes";
    private final String labelName;
    private final Map<String, Object> query = new HashMap<>();

    public GetNodesWithLabelRequest(String labelName) {
        this.labelName = labelName;
    }

    public GetNodesWithLabelRequest(String labelName, Map<String, Object> query) {
        this.labelName = labelName;
        this.query.putAll(query);
    }

    @Override
    protected String getPath() {
        return String.format(PATH_FORMAT, labelName);
    }

    @Override
    protected Map<String, Object> getQueryParams() {
        return query;
    }

    @Override
    protected Node[] parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException {
        try {
            final JSONArray array = (JSONArray) JSONValue.parseWithException(responseContent);
            Node[] nodes = new Node[array.size()];
            for (ListIterator it = array.listIterator(); it.hasNext();) {
                nodes[it.nextIndex()] = new Node((JSONObject) it.next());
            }
            return nodes;
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
