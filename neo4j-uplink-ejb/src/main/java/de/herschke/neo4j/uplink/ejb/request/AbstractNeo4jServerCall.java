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

import de.herschke.neo4j.uplink.ejb.response.conversion.Neo4jServerResponseException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.json.simple.JSONValue;

/**
 *
 * @author rhk
 */
public abstract class AbstractNeo4jServerCall<Res> implements Neo4jServerCall<Res> {

    private final EnumSet<ClientResponse.Status> expectedStatus;

    protected AbstractNeo4jServerCall() {
        this(EnumSet.of(ClientResponse.Status.OK));
    }

    protected AbstractNeo4jServerCall(ClientResponse.Status expectedStatus) {
        this.expectedStatus = EnumSet.of(expectedStatus);
    }

    protected AbstractNeo4jServerCall(EnumSet<ClientResponse.Status> expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    /**
     * returns the method to be used when calling the server.
     * <p>
     * the default value is
     * <code>POST<code>
     *
     * @return the method
     */
    protected String getMethod() {
        return "POST";
    }

    /**
     * returns the path where to execute the call.
     *
     * @return
     */
    protected abstract String getPath();

    /**
     * parses the response.
     *
     * @param status the status code of the response
     * @param responseContent the content of the response as an {@link Reader}
     * @return the result of type {@literal R}
     */
    protected abstract Res parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException;

    /**
     * returns the entity to be used for the request or null if no entity has to
     * be used.
     *
     * @return an instance of &lt;String&gt; or null
     */
    protected abstract String getRequestEntity() throws Neo4jServerException;

    /**
     * returns the additional queryParams for this call.
     *
     * @return
     */
    protected Map<String, Object> getQueryParams() {
        return Collections.emptyMap();
    }

    @Override
    public Res execute(WebResource serverResource) throws Neo4jServerException {
        WebResource resource = serverResource.path(getPath());
        for (Map.Entry<String, Object> entry : getQueryParams().entrySet()) {
            resource = resource.queryParam(entry.getKey(), JSONValue.toJSONString(entry.getValue()));
        }
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).method(getMethod(), ClientResponse.class, getRequestEntity());
        if (this.expectedStatus.contains(response.getClientResponseStatus())) {
            return parseResponse(response.getClientResponseStatus(), new LoggingReader(new InputStreamReader(response.getEntityInputStream(), ENCODING)));
        } else if (response.getClientResponseStatus() == ClientResponse.Status.BAD_REQUEST) {
            throw Neo4jServerResponseException.fromServerResponse(new LoggingReader(new InputStreamReader(response.getEntityInputStream(), ENCODING)));
        } else {
            throw new Neo4jServerException(String.format("call to Neo4j Server result in response with status: %s reason: %s%n%s%n", response.getClientResponseStatus(), response.getClientResponseStatus().getReasonPhrase(), response));
        }
    }
}
