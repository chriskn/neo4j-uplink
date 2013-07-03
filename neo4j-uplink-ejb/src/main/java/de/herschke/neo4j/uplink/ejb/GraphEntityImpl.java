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

import de.herschke.neo4j.uplink.api.GraphEntity;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;

/**
 * the base class for node or relationships of a graph
 *
 * @author rhk
 */
public abstract class GraphEntityImpl implements Serializable, GraphEntity {

    protected static final Pattern selfUrlPattern = Pattern.compile("http://.+/db/data/(node|relationship)/(\\d+)");
    private final int id;
    protected final JSONObject entity;

    public GraphEntityImpl(String type, JSONObject entity) {
        if (!entity.containsKey("self") || !entity.containsKey("data")) {
            throw new IllegalArgumentException("given map is not a graphEntity, must contain 'self' and 'data' entry!");
        }
        String selfUrl = (String) entity.get("self");
        Matcher m = selfUrlPattern.matcher(selfUrl);
        if (m.matches()) {
            if (type.equalsIgnoreCase(m.group(1))) {
                this.id = Integer.parseInt(m.group(2));
                this.entity = entity;
            } else {
                throw new IllegalArgumentException("map is not of type: " + type);
            }
        } else {
            throw new IllegalArgumentException("self entry of map must match: " + selfUrlPattern.pattern());
        }
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public Object getPropertyValue(String name) {
        return ((JSONObject) entity.get("data")).get(name);
    }

    @Override
    public boolean hasProperty(String name) {
        return ((JSONObject) entity.get("data")).containsKey(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        return Collections.unmodifiableSet(((JSONObject) entity.get("data"))
                .keySet());
    }
}
