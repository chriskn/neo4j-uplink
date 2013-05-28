package de.herschke.neo4j.uplink.ejb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import de.herschke.neo4j.uplink.api.CypherException;
import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import de.herschke.neo4j.uplink.ejb.responsehandling.CypherResponseHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ws.rs.core.MediaType;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
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
                return parseCypherResponse(response.getEntityInputStream());
            } catch (IOException | ParseException ex) {
                throw new CypherException("an unparseable response was retrieved: " + ex.getMessage(), ex);
            }
        } else if (response.getClientResponseStatus().equals(ClientResponse.Status.BAD_REQUEST)) {
            throw parseCypherError(response);
        } else {
            throw new CypherException(String.format("call to Neo4j Server result in response with status: %s reason: %s%n%s%n", response.getClientResponseStatus(), response.getClientResponseStatus().getReasonPhrase(), response));
        }
    }

    private String buildIndexRequest(String indexName, Map<String, Object> config) {
        JSONObject indexRequest = new JSONObject();
        indexRequest.put("name", indexName);
        indexRequest.put("config", config);
        return indexRequest.toJSONString();
    }

    private String buildCypherRequest(String query, Map<String, Object> params) throws CypherException {
        JSONObject cypherRequest = new JSONObject();
        cypherRequest.put("query", query);
        Map<String, Object> _params = new HashMap<>();
        for (Entry<String, Object> entry : params.entrySet()) {
            toJSONObject(_params, entry.getKey(), entry.getValue());
        }
        cypherRequest.put("params", _params);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(cypherRequest.toJSONString());
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        return cypherRequest.toJSONString();
    }

    private CypherResult parseCypherResponse(InputStream eis) throws ParseException, IOException {
        final CypherResponseHandler handler = new CypherResponseHandler();
        JSONParser parser = new JSONParser();
        parser.parse(new InputStreamReader(eis, "UTF-8"), handler);
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

    private void toJSONObject(Map<String, Object> params, String prefix, Object value) throws CypherException {
        if (value == null || value instanceof String || value instanceof Double || value instanceof Float || value instanceof Number || value instanceof Boolean || value instanceof JSONStreamAware || value instanceof JSONAware || value instanceof Map || value instanceof List) {
            params.put(prefix, value);
        } else if (value instanceof Class) {
            params.put(prefix, ((Class) value).getSimpleName());
        } else {
            try {
                JSONObject object = new JSONObject();
                for (Method method : value.getClass().getMethods()) {
                    if (!"getClass".equals(method.getName()) && (method.getName().startsWith("get") || method.getName().startsWith("is")) && method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                        String name = method.getName().startsWith("get") ? (method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4)) : (method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3));
                        final Object newValue = method.invoke(value);
                        toJSONObject(params, prefix + "." + name, newValue);
                        object.put(name, newValue);
                    }
                }
                params.put(prefix, object);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new CypherException(String.format("cannot build cypher-query, due to: %s(%s)", ex.getClass().getSimpleName(), ex.getMessage()), ex);
            }
        }
    }
}
