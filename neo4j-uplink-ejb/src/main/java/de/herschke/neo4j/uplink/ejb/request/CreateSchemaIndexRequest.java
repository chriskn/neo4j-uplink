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
import java.io.Reader;
import java.util.Arrays;
import org.json.simple.JSONObject;

/**
 * creates a new schema index for nodes with property keys.
 *
 * @author rhk
 */
public class CreateSchemaIndexRequest extends AbstractNeo4jServerCall<Boolean> {

    /**
     * the URI path where the server can create indexes
     */
    public static final String PATH_FORMAT = "schema/index/%s";
    private final String indexName;
    private final String[] propertyNames;

    public CreateSchemaIndexRequest(String indexName, String... propertyNames) {
        this.indexName = indexName;
        this.propertyNames = propertyNames;
    }

    @Override
    protected String getRequestEntity() throws Neo4jServerException {
        JSONObject object = new JSONObject();
        object.put("property_keys", Arrays.asList(propertyNames));
        return object.toJSONString();
    }

    @Override
    protected String getPath() {
        return String.format(PATH_FORMAT, indexName);
    }

    @Override
    protected Boolean parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException {
        return true; // parseResponse is pnly called, when status == CREATED due to the constructor.
    }
}
