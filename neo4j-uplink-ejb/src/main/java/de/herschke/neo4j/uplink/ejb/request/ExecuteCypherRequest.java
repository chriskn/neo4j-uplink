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
import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.ejb.response.handling.CypherResponseHandler;
import de.herschke.neo4j.uplink.ejb.utils.JSONHelper;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * executes the capher.
 *
 * @author rhk
 */
public class ExecuteCypherRequest extends AbstractNeo4jServerCall<CypherResult> {

    /**
     * the URI path where the server can execute cypher queries
     */
    public static final String PATH = "cypher";
    private final String query;
    private final Map<String, Object> params = new HashMap<>();

    public ExecuteCypherRequest(String query) {
        this.query = query;
    }

    public ExecuteCypherRequest(String query, Map<String, Object> params) {
        this.query = query;
        this.params.putAll(params);
    }

    public ExecuteCypherRequest withParameter(String name, Object value) {
        this.params.put(name, value);
        return this;
    }

    @Override
    protected String getRequestEntity() throws Neo4jServerException {
        JSONObject object = new JSONObject();
        object.put("query", query);
        object.put("params", JSONHelper.toDeepJSONObject(params));
        return object.toJSONString();
    }

    @Override
    protected String getPath() {
        return PATH;
    }

    @Override
    protected CypherResult parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException {
        switch (status) {
            case OK:
                return parseCypherResponse(responseContent);
            default:
                throw new IllegalStateException(String.format("%s must not call parseResponse for status: %s", getClass().getSuperclass().getSimpleName(), status.name()));
        }
    }

    private CypherResult parseCypherResponse(Reader response) throws Neo4jServerException {
        try {
            final CypherResponseHandler handler = new CypherResponseHandler();
            JSONParser parser = new JSONParser();
            parser.parse(response, handler);
            return handler.getResult();
        } catch (IOException | ParseException ex) {
            throw new Neo4jServerException(String.format("cannot parse the cypher response: %s", ex.getMessage()), ex);
        }
    }
}
