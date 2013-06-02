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

import de.herschke.neo4j.uplink.api.GraphEntity;
import de.herschke.neo4j.uplink.ejb.utils.ResultHelper;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rhk
 */
public class MapInvocationHandler<P> extends AbstractInvocationHandler<P> {

    private final Map<String, Object> data;
    private final AbstractInvocationHandler<P> fallback;

    public MapInvocationHandler(Map<String, Object> data, AbstractInvocationHandler<P> fallback) {
        this.data = data;
        this.fallback = fallback;
    }

    @Override
    protected Object getPropertyValue(String property) {
        if (this.data.containsKey(property)) {
            return this.data.get(property);
        } else if (fallback != null) {
            return fallback.getPropertyValue(property);
        } else {
            return null;
        }
    }

    @Override
    protected <R> R createProxyObject(Class<R> returnType, String property) {
        Object value = getPropertyValue(property);
        if (value == null) {
            return null;
        } else if (value instanceof GraphEntity) {
            return (R) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{returnType}, new GraphEntityInvocationHandler((GraphEntity) value, this));
        } else {
            Map<String, Object> subData = new HashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (entry.getKey().startsWith(property + ".")) {
                    subData.put(entry.getKey().substring(property.length() + 1), entry.getValue());
                }
            }
            return (R) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{returnType}, new MapInvocationHandler(subData, this));
        }
    }

    @Override
    protected <E> List<E> createProxyList(Class<E> componentType, String property) {
        return new ProxyList(componentType, ResultHelper.invert(data, property + "."));
    }
}
