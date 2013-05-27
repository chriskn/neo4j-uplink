package de.herschke.neo4j.uplink.api;

import java.util.Map;

/**
 * this is the Uplink API interface
 *
 * @author rhk
 */
public interface Neo4jUplink {

    boolean createNodeIndex(String name, Map<String, Object> config) throws Exception;

    CypherResult executeCypherQuery(String query, Map<String, Object> params) throws Exception;
}
