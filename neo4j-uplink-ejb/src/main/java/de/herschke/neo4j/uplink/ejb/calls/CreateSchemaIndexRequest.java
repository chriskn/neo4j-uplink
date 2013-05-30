package de.herschke.neo4j.uplink.ejb.calls;

import com.sun.jersey.api.client.ClientResponse;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import java.io.Reader;
import java.util.Arrays;
import org.json.simple.JSONObject;

/**
 * creates a new schema index for nodes with property keys.
 *
 * @author rhk
 */
public class CreateSchemaIndexRequest extends AbstractNeo4jServerCall<Boolean> {

    /**
     * the URI path where the server can create indexes
     */
    public static final String PATH_FORMAT = "schema/index/%s";
    private final String indexName;
    private final String[] propertyNames;

    public CreateSchemaIndexRequest(String indexName, String... propertyNames) {
        this.indexName = indexName;
        this.propertyNames = propertyNames;
    }

    @Override
    protected String getRequestEntity() throws Neo4jServerException {
        JSONObject object = new JSONObject();
        object.put("property_keys", Arrays.asList(propertyNames));
        return object.toJSONString();
    }

    @Override
    protected String getPath() {
        return String.format(PATH_FORMAT, indexName);
    }

    @Override
    protected Boolean parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException {
        return true; // parseResponse is pnly called, when status == CREATED due to the constructor.
    }
}
