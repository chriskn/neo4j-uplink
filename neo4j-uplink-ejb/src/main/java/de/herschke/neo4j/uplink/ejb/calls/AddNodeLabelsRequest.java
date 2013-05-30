package de.herschke.neo4j.uplink.ejb.calls;

import com.sun.jersey.api.client.ClientResponse;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import java.io.Reader;
import java.util.Arrays;
import org.json.simple.JSONArray;

/**
 * creates labels for nodes.
 *
 * @author rhk
 */
public class AddNodeLabelsRequest extends AbstractNeo4jServerCall<Boolean> {

    /**
     * the URI path where the server can create labels for a node
     */
    public static final String PATH_FORMAT = "node/%d/labels";
    private final int nodeId;
    private final String[] labelNames;

    public AddNodeLabelsRequest(int nodeId, String... labelNames) {
        super(ClientResponse.Status.NO_CONTENT);
        this.nodeId = nodeId;
        this.labelNames = labelNames;
    }

    @Override
    protected String getRequestEntity() throws Neo4jServerException {
        switch (labelNames.length) {
            case 0:
                throw new Neo4jServerException("must specify label name(s)");
            case 1:
                return "\"" + labelNames[0] + "\"";
            default:
                JSONArray array = new JSONArray();
                array.addAll(Arrays.asList(labelNames));
                return array.toJSONString();
        }
    }

    @Override
    protected String getPath() {
        return String.format(PATH_FORMAT, nodeId);
    }

    @Override
    protected Boolean parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException {
        return true;
    }
}
