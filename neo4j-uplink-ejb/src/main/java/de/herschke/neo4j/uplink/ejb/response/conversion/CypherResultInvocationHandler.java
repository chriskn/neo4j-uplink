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

import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.api.GraphEntity;
import de.herschke.neo4j.uplink.ejb.utils.ResultHelper;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rhk
 */
public class CypherResultInvocationHandler<P> extends AbstractInvocationHandler<P> {

    private final CypherResult result;
    private final String prefix;
    private final int rowIndex;
    private GraphEntity entity = null; // stores the `this` graph entity for fast reuse.

    public CypherResultInvocationHandler(String prefix, CypherResult result, int rowIndex) {
        this.prefix = prefix;
        this.result = result;
        this.rowIndex = rowIndex;
    }

    @Override
    protected Object getPropertyValue(String property) {
        // if `this` entity was not cached before...
        if (this.entity == null) {
            // looking for `this` column in the result set...
            if (result.getColumnNames().contains("this")) {
                // get the `this` column value
                Object value = result.getValue(rowIndex, "this");
                // `this` value must be a GraphEntity!
                if (!(value instanceof GraphEntity)) {
                    throw new IllegalStateException(String.format("when using `this` in the cypher return value, then the result must be either a node or a relationship, but: %s is not!", value));
                }
                // remember the entity
                this.entity = (GraphEntity) value;
            }
        }
        Object value = null;
        // check, if `this` entity exists and has the desired property...
        if (this.entity != null && this.entity.hasProperty(property)) {
            // return the property
            value = this.entity.getPropertyValue(property);
        } else if (result.getColumnNames().contains(prefix + property)) {
            // result contains no `this` or `this`-entity does not have the desired property.
            value = result.getValue(rowIndex, prefix + property);
        }
        return value;
    }

    private <R> R createProxyForValue(Class<R> returnType, String property, Object value) throws IllegalArgumentException {
        if (value != null && value instanceof GraphEntity) {
            return (R) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{returnType}, new GraphEntityInvocationHandler((GraphEntity) value, this));
        } else if (value != null && value instanceof Iterable) {
            for (Object element : (Iterable) value) {
                return createProxyForValue(returnType, property, element);
            }
            return null;
        } else {
            Map<String, Object> subData = new HashMap<>();
            for (String column : this.result.getColumnNames()) {
                if (column.startsWith(property + ".")) {
                    subData.put(column.substring(property.length() + 1), result.getValue(rowIndex, column));
                }
            }
            return (R) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{returnType}, new MapInvocationHandler(subData, this));
        }
    }

    @Override
    protected <R> R createProxyObject(Class<R> returnType, String property) throws IllegalArgumentException {
        Object value = getPropertyValue(property);
        return createProxyForValue(returnType, property, value);
    }

    @Override
    protected <E> List<E> createProxyList(Class<E> componentType, String property) {
        if (this.result.getColumnNames().contains(property)) {
            Object value = getPropertyValue(property);

            if (value == null) {
                return null;
            }

            if (Iterable.class.isAssignableFrom(value.getClass())) {
                final List<E> list = new ArrayList<>();
                for (Object element : ((Iterable) value)) {
                    list.add(createProxyForValue(componentType, property, element));
                }
                return list;
            } else {
                return Collections.singletonList(createProxyObject(componentType, property));
            }
        } else {
            return new ProxyList(componentType, ResultHelper.invert(this.result.getRowData(rowIndex), property + "."));
        }
    }

    @Override
    public String toString() {
        return "CypherResult: " + this.result;
    }
}
