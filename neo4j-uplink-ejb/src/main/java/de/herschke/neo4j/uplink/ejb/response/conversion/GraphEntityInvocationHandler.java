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
import de.herschke.neo4j.uplink.api.Node;
import de.herschke.neo4j.uplink.api.Relationship;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author rhk
 */
public class GraphEntityInvocationHandler<P> extends AbstractInvocationHandler<P> {

    private final GraphEntity entity;
    private final AbstractInvocationHandler<P> fallback;

    private GraphEntityInvocationHandler(GraphEntity entity, AbstractInvocationHandler<P> fallback) {
        this.entity = entity;
        this.fallback = fallback;
    }

    @Override
    protected Object getPropertyValue(String property) {
        if (this.entity.hasProperty(property)) {
            return this.entity.getPropertyValue(property);
        } else if (fallback != null) {
            return fallback.getPropertyValue(property);
        } else {
            return null;
        }
    }

    @Override
    protected <R> R createProxyObject(Class<R> returnType, String property) {
        return createProxyForValue(returnType, property, getPropertyValue(property));
    }

    @Override
    protected <E> List<E> createProxyList(final Class<E> componentType, final String property) {
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
            return Collections.singletonList(createProxyForValue(componentType, property, value));
        }
    }

    private <R> R createProxyForValue(Class<R> returnType, String property, Object value) throws IllegalArgumentException, ClassCastException {
        if (value == null) {
            return null;
        } else if (value instanceof GraphEntity) {
            return createGraphEntityProxy(returnType, (GraphEntity) value, this);
        } else {
            throw new ClassCastException("cannot convert: " + value.getClass().getSimpleName() + " to an instance of: " + returnType.getSimpleName() + " for property: " + property);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Node.class && entity instanceof Node) {
            // delegate to the entity
            return method.invoke(entity, args);
        } else if (method.getDeclaringClass() == Relationship.class && entity instanceof Relationship) {
            // delegate to the entity
            return method.invoke(entity, args);
        } else if (method.getDeclaringClass() == GraphEntity.class) {
            // delegate to the entity
            return method.invoke(entity, args);
        }
        return super.invoke(proxy, method, args); //To change body of generated methods, choose Tools | Templates.
    }

    public static <R> R createGraphEntityProxy(Class<R> returnType, GraphEntity value, AbstractInvocationHandler fallback) throws IllegalArgumentException {
        if (value instanceof Node) {
            return (R) Proxy.newProxyInstance(returnType.getClassLoader(), new Class[]{returnType, Node.class}, new GraphEntityInvocationHandler(value, fallback));

        } else if (value instanceof Relationship) {
            return (R) Proxy.newProxyInstance(returnType.getClassLoader(), new Class[]{returnType, Relationship.class}, new GraphEntityInvocationHandler(value, fallback));

        } else {
            throw new IllegalArgumentException("unknown graph entity type: " + value.getClass().getSimpleName());
        }
    }
}
