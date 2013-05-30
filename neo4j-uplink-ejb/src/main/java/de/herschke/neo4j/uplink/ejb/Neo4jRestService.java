package de.herschke.neo4j.uplink.ejb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import de.herschke.neo4j.uplink.api.Node;
import de.herschke.neo4j.uplink.ejb.calls.AddNodeLabelsRequest;
import de.herschke.neo4j.uplink.ejb.calls.CreateNodeIndexRequest;
import de.herschke.neo4j.uplink.ejb.calls.ExecuteCypherRequest;
import de.herschke.neo4j.uplink.ejb.calls.GetNodeLabelsRequest;
import de.herschke.neo4j.uplink.ejb.calls.GetNodesWithLabelRequest;
import de.herschke.neo4j.uplink.ejb.calls.Neo4jServerCall;
import java.util.Collections;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;

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

    private <R> R executeServerCall(Neo4jServerCall<R> call) throws Neo4jServerException {
        return call.execute(clientResource);
    }

    @Override
    public boolean createNodeIndex(String name, Map<String, Object> config) throws Neo4jServerException {
        return executeServerCall(new CreateNodeIndexRequest(name, config));
    }

    @Override
    public CypherResult executeCypherQuery(String query) throws Neo4jServerException {
        return executeServerCall(new ExecuteCypherRequest(query));
    }

    @Override
    public CypherResult executeCypherQuery(String query, Map<String, Object> params) throws Neo4jServerException {
        return executeServerCall(new ExecuteCypherRequest(query, params));
    }

    @Override
    public String[] getNodeLabels(int nodeId) throws Neo4jServerException {
        return executeServerCall(new GetNodeLabelsRequest(nodeId));
    }

    @Override
    public boolean addNodeLabel(int nodeId, String... labels) throws Neo4jServerException {
        return executeServerCall(new AddNodeLabelsRequest(nodeId, labels));
    }

    @Override
    public Node[] getNodesWithLabel(String labelName) throws Neo4jServerException {
        return getNodesWithLabelAndProperties(labelName, Collections.<String, Object>emptyMap());
    }

    @Override
    public Node[] getNodesWithLabelAndProperties(String labelName, Map<String, Object> properties) throws Neo4jServerException {
        return executeServerCall(new GetNodesWithLabelRequest(labelName, properties));
    }
}
