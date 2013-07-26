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
package de.herschke.neo4j.uplink.ejb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import de.herschke.neo4j.uplink.api.Node;
import de.herschke.neo4j.uplink.ejb.request.AddNodeLabelsRequest;
import de.herschke.neo4j.uplink.ejb.request.CreateNodeIndexRequest;
import de.herschke.neo4j.uplink.ejb.request.CreateSchemaIndexRequest;
import de.herschke.neo4j.uplink.ejb.request.ExecuteCypherRequest;
import de.herschke.neo4j.uplink.ejb.request.GetNodeLabelsRequest;
import de.herschke.neo4j.uplink.ejb.request.GetNodesWithLabelRequest;
import de.herschke.neo4j.uplink.ejb.request.Neo4jServerCall;
import de.herschke.neo4j.uplink.ejb.utils.ResultList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 *
 * @author rhk
 */
@Stateless
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
    public boolean createSchemaIndex(String name, String... propertyKeys) throws Neo4jServerException {
        return executeServerCall(new CreateSchemaIndexRequest(name, propertyKeys));
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

    @Override
    public <T> List<T> executeCypherQuery(Class<T> type, String query) throws Neo4jServerException {
        return executeCypherQuery(type, query, Collections.<String, Object>emptyMap());
    }

    @Override
    public <T> List<T> executeCypherQuery(Class<T> type, String query, Map<String, Object> params) throws Neo4jServerException {
        final CypherResult result = executeCypherQuery(query, params);
        if (result == null) {
            return null;
        } else if (type.isInterface()) {
            return convertResult(type, result);
        }
        throw new IllegalArgumentException("Type for conversion must be an interface!");
    }

    private <T> List<T> convertResult(final Class<T> type, final CypherResult result) {
        return new ResultList(result, type);
    }
}
