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
package de.herschke.neo4j.uplink.ejb.response.conversion;

import de.herschke.neo4j.uplink.api.Neo4jServerException;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rhk
 */
public class Neo4jServerResponseException extends Neo4jServerException {

    private final String type;
    private final String message;
    private final List<String> stacktrace;

    private Neo4jServerResponseException(String type, String message, List<String> stacktrace) {
        super(String.format("%s: %s", type, message));
        this.type = type;
        this.message = message;
        this.stacktrace = stacktrace;
    }

    public String getServerMessage() {
        return this.message;
    }

    public String getType() {
        return this.type;
    }

    public List<String> getServerStacktrace() {
        return this.stacktrace;
    }

    public static Neo4jServerResponseException fromServerResponse(Reader response) {
        try {
            JSONObject result = (JSONObject) JSONValue.parseWithException(response);
            return new Neo4jServerResponseException((String) result.get("exception"), (String) result.get("message"), (List<String>) result.get("stacktrace"));
        } catch (IOException | ParseException ex) {
            throw new IllegalArgumentException("cannot parse the Server Response: " + ex.getMessage(), ex);
        }
    }
}
