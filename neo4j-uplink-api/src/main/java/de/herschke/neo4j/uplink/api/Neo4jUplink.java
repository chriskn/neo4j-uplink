/*
 * This file is part of the Uplink Framework for Neo4j Server Connection.
 *
 * Copyright (c) by:
 *
 * Robert Herschke
 * Hauptstrasse 30
 * 65760 Eschborn
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.herschke.neo4j.uplink.api;

import java.util.List;
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
     * executes the given cypher query and converts the cypher response to the
     * given type.
     *
     * @param type the type of the result
     * @param query the cypher query
     * @return a {@link CypherResult} that represents the result of the cypher
     * query
     * @throws Neo4jServerException if something went wrong on the neo4j server
     */
    <T> List<T> executeCypherQuery(Class<T> type, String query) throws Neo4jServerException;

    /**
     * executes the given cypher query and uses the parameters. Finally it
     * converts the cypher response to the specified type.
     *
     * @param type the type of the result
     * @param query the cypher query
     * @param params the params for the query
     * @return a {@link CypherResult} that represents the result of the cypher
     * query
     * @throws Neo4jServerException if something went wrong on the neo4j server
     */
    <T> List<T> executeCypherQuery(Class<T> type, String query, Map<String, Object> params) throws Neo4jServerException;

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
