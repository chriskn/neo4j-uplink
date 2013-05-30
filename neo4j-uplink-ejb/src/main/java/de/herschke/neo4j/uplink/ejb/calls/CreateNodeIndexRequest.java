package de.herschke.neo4j.uplink.ejb.calls;

import com.sun.jersey.api.client.ClientResponse;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import org.json.simple.JSONObject;

/**
 * creates a new index for nodes.
 *
 * @author rhk
 */
public class CreateNodeIndexRequest extends AbstractNeo4jServerCall<Boolean> {

    /**
     * the URI path where the server can create indexes
     */
    public static final String PATH = "index/node";
    private final String indexName;
    private final Map<String, Object> config;

    public CreateNodeIndexRequest(String indexName) {
        this(indexName, Collections.<String, Object>emptyMap());
    }

    public CreateNodeIndexRequest(String indexName, Map<String, Object> config) {
        super(ClientResponse.Status.CREATED);
        this.indexName = indexName;
        this.config = config;
    }

    @Override
    protected String getRequestEntity() throws Neo4jServerException {
        JSONObject object = new JSONObject();
        object.put("name", indexName);
        object.put("config", config);
        return object.toJSONString();
    }

    @Override
    protected String getPath() {
        return PATH;
    }

    @Override
    protected Boolean parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException {
        return true; // parseResponse is pnly called, when status == CREATED due to the constructor.
    }
}
