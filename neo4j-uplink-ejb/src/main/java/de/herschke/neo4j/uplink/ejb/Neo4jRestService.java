package de.herschke.neo4j.uplink.ejb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.ejb.responsehandling.CypherResponseHandler;
import java.io.IOException;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ws.rs.core.MediaType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rhk
 */
@Singleton
public class Neo4jRestService {

    @Resource(name = "neo4j-server-url")
    String neo4jURL;
    private Client client = Client.create();
    private WebResource clientResource;

    @PostConstruct
    void init() {
        if (neo4jURL == null || neo4jURL.trim().length() == 0) {
            throw new IllegalArgumentException("Env-Entry 'neo4j-server-url' must be specified in the format http://host:port/db/data!");
        }
        client.setFollowRedirects(true);
        clientResource = client.resource(neo4jURL);
    }

    public boolean createNodeIndex(String name, Map<String, Object> config) throws Exception {
        ClientResponse response = clientResource.path("index/node").accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, buildIndexRequest(name, config));
        return response.getClientResponseStatus() == ClientResponse.Status.CREATED;
    }

    public CypherResult executeCypherQuery(String query, Map<String, Object> params) throws Exception {
        ClientResponse response = clientResource.path("cypher").accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, buildCypherRequest(query, params));

        if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            return parseCypherResponse(response.getEntity(String.class));
        } else {
            throw parseCypherError(response);
        }
    }

    private String buildIndexRequest(String indexName, Map<String, Object> config) throws IOException {
        JSONObject indexRequest = new JSONObject();
        indexRequest.put("name", indexName);
        indexRequest.put("config", config);
        return indexRequest.toJSONString();
    }

    private String buildCypherRequest(String query, Map<String, Object> params) throws IOException {
        JSONObject cypherRequest = new JSONObject();
        cypherRequest.put("query", query);
        cypherRequest.put("params", params);
        return cypherRequest.toJSONString();
    }

    private CypherResult parseCypherResponse(String eis) throws IOException {
        try {
            final CypherResponseHandler handler = new CypherResponseHandler();
            JSONParser parser = new JSONParser();
            System.out.println(eis);
            //parser.parse(new InputStreamReader(eis, "UTF-8"), handler);
            parser.parse(eis, handler);
            return handler.getResult();
        } catch (ParseException ex) {
            throw new IOException(String.format("cannot parse the result: %s", ex.getMessage()), ex);
        }
    }

    private Exception parseCypherError(ClientResponse response) throws IOException {
        String message = "";
        String exception = "";
        return new Exception(String.format("Cypher-Exception: %s(%s)", exception, message));
    }
}
