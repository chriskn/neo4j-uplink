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

import de.herschke.neo4j.uplink.api.Relationship;
import java.util.regex.Matcher;
import org.json.simple.JSONObject;

/**
 * a simple relationship object.
 *
 * @author rhk
 */
public class RelationshipImpl extends GraphEntityImpl implements Relationship {

    private final int startId, endId;

    public RelationshipImpl(JSONObject entity) {
        super("relationship", entity);
        Matcher m = selfUrlPattern.matcher((String) entity.get("start"));
        if (m.matches()) {
            if ("node".equalsIgnoreCase(m.group(1))) {
                this.startId = Integer.parseInt(m.group(2));
            } else {
                throw new IllegalArgumentException("start of relationship is not a node");
            }
        } else {
            throw new IllegalArgumentException("start url must match: " + selfUrlPattern.pattern());
        }
        m = selfUrlPattern.matcher((String) entity.get("end"));
        if (m.matches()) {
            if ("node".equalsIgnoreCase(m.group(1))) {
                this.endId = Integer.parseInt(m.group(2));
            } else {
                throw new IllegalArgumentException("end of relationship is not a node");
            }
        } else {
            throw new IllegalArgumentException("end url must match: " + selfUrlPattern.pattern());
        }
    }

    @Override
    public String getType() {
        return (String) entity.get("type");
    }

    @Override
    public int getStartId() {
        return startId;
    }

    @Override
    public int getEndId() {
        return endId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(getStartId());
        sb.append(")-[");
        sb.append(getId());
        sb.append(":");
        sb.append(getType());
        sb.append(" ");
        sb.append(entity.get("data"));
        sb.append("]->(");
        sb.append(getEndId());
        sb.append(")");
        return sb.toString();
    }
}
