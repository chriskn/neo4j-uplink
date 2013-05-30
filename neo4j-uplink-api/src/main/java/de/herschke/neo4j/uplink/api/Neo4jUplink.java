package de.herschke.neo4j.uplink.api;

import java.util.Map;

/**
 * this is the Uplink API interface
 *
 * @author rhk
 */
public interface Neo4jUplink {

    /**
     * creates an index for nodes
     *
     * @param name the name of the index
     * @param config the configuration of this index
     * @return true, if the index was created.
     * @throws Neo4jServerException if something went wrong on the neo4j server
     */
    boolean createNodeIndex(String name, Map<String, Object> config) throws Neo4jServerException;

    /**
     * creates a new schema index with the given propertyKeys.
     *
     * @param name the schema index name
     * @param propertyKeys an array of property names
     * @return true, if the index was created
     * @throws Neo4jServerException
     */
    boolean createSchemaIndex(String name, String... propertyKeys) throws Neo4jServerException;

    /**
     * executes the given cypher query
     *
     * @param query the cypher query
     * @return a {@link CypherResult} that represents the result of the cypher
     * query
     * @throws Neo4jServerException if something went wrong on the neo4j server
     */
    CypherResult executeCypherQuery(String query) throws Neo4jServerException;

    /**
     * executes the given cypher query and uses the parameters.
     *
     * @param query the cypher query
     * @param params the params for the query
     * @return a {@link CypherResult} that represents the result of the cypher
     * query
     * @throws Neo4jServerException if something went wrong on the neo4j server
     */
    CypherResult executeCypherQuery(String query, Map<String, Object> params) throws Neo4jServerException;

    /**
     * returns the labels for a specific node.
     *
     * @param nodeId the id of the node
     * @return the labels as an array of {@link String}s
     * @throws Neo4jServerException if something went wrong on the neo4j server
     */
    String[] getNodeLabels(int nodeId) throws Neo4jServerException;

    /**
     * adds the given labels to a specific node
     *
     * @param nodeId the id of the node to set the label
     * @param labels the name of the labels to set
     * @return true, if the label was added
     * @throws Neo4jServerException if something went wrong on the neo4j server
     */
    boolean addNodeLabel(int nodeId, String... labels) throws Neo4jServerException;

    /**
     * return all the nodes, that have a specific label
     *
     * @param labelName the name of the label
     * @return an array of nodes.
     * @throws Neo4jServerException
     */
    Node[] getNodesWithLabel(String labelName) throws Neo4jServerException;

    /**
     * return all the nodes, that have a specific label and has the given
     * property values.
     *
     * @param labelName the name of the label
     * @param properties the properties to check for
     * @return an array of nodes.
     * @throws Neo4jServerException
     */
    Node[] getNodesWithLabelAndProperties(String labelName, Map<String, Object> properties) throws Neo4jServerException;
}
