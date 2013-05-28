package de.herschke.neo4j.uplink.ejb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import de.herschke.neo4j.uplink.api.CypherException;
import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import de.herschke.neo4j.uplink.ejb.responsehandling.CypherResponseHandler;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ws.rs.core.MediaType;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rhk
 */
@Singleton
@Local(Neo4jUplink.class)
public class Neo4jRestService implements Neo4jUplink {

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

    @Override
    public boolean createNodeIndex(String name, Map<String, Object> config) throws CypherException {
        ClientResponse response = clientResource.path("index/node").accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, buildIndexRequest(name, config));
        return response.getClientResponseStatus() == ClientResponse.Status.CREATED;
    }

    @Override
    public CypherResult executeCypherQuery(String query, Map<String, Object> params) throws CypherException {
        ClientResponse response = clientResource.path("cypher").accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, buildCypherRequest(query, params));

        if (response.getClientResponseStatus().equals(ClientResponse.Status.OK)) {
            try {
                return parseCypherResponse(response.getEntity(String.class));
            } catch (ParseException ex) {
                throw new CypherException("an unparseable response was retrieved: " + ex.getMessage());
            }
        } else if (response.getClientResponseStatus().equals(ClientResponse.Status.BAD_REQUEST)) {
            throw parseCypherError(response);
        } else {
            throw new CypherException(String.format("call to Neo4j Server result in response with status: %s reason: %s", response.getClientResponseStatus(), response.getClientResponseStatus().getReasonPhrase()));
        }
    }

    private String buildIndexRequest(String indexName, Map<String, Object> config) {
        JSONObject indexRequest = new JSONObject();
        indexRequest.put("name", indexName);
        indexRequest.put("config", config);
        return indexRequest.toJSONString();
    }

    private String buildCypherRequest(String query, Map<String, Object> params) {
        JSONObject cypherRequest = new JSONObject();
        cypherRequest.put("query", query);
        cypherRequest.put("params", params);
        return cypherRequest.toJSONString();
    }

    private CypherResult parseCypherResponse(String eis) throws ParseException {
        final CypherResponseHandler handler = new CypherResponseHandler();
        JSONParser parser = new JSONParser();
        System.out.println(eis);
        //parser.parse(new InputStreamReader(eis, "UTF-8"), handler);
        parser.parse(eis, handler);
        return handler.getResult();
    }

    private CypherException parseCypherError(ClientResponse response) {
        JSONObject result = (JSONObject) JSONValue.parse(response.getEntity(String.class));
        if (result != null) {
            return new CypherException(String.format("Cypher-Exception: %s(%s)", result.get("exception"), result.get("message")));
        } else {
            return new CypherException("caught a Cypher-Exception without an exception entity");
        }
    }
}
