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
package de.herschke.neo4j.uplink.ejb.request;

import com.sun.jersey.api.client.ClientResponse;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.api.Node;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * creates labels for nodes.
 *
 * @author rhk
 */
public class GetNodesWithLabelRequest extends AbstractNeo4jServerCall<Node[]> {

    /**
     * the URI path where the server can get the nodes for the label
     */
    public static final String PATH_FORMAT = "label/%s/nodes";
    private final String labelName;
    private final Map<String, Object> query = new HashMap<>();

    public GetNodesWithLabelRequest(String labelName) {
        this.labelName = labelName;
    }

    public GetNodesWithLabelRequest(String labelName, Map<String, Object> query) {
        this.labelName = labelName;
        this.query.putAll(query);
    }

    @Override
    protected String getPath() {
        return String.format(PATH_FORMAT, labelName);
    }

    @Override
    protected Map<String, Object> getQueryParams() {
        return query;
    }

    @Override
    protected Node[] parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException {
        try {
            final JSONArray array = (JSONArray) JSONValue.parseWithException(responseContent);
            Node[] nodes = new Node[array.size()];
            for (ListIterator it = array.listIterator(); it.hasNext();) {
                nodes[it.nextIndex()] = new Node((JSONObject) it.next());
            }
            return nodes;
        } catch (IOException | ParseException ex) {
            throw new Neo4jServerException("cannot parse the response: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected String getRequestEntity() throws Neo4jServerException {
        return null;
    }

    @Override
    protected String getMethod() {
        return "GET";
    }
}
