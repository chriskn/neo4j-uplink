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

import de.herschke.neo4j.uplink.ejb.utils.ResultHelper;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author rhk
 */
public abstract class AbstractInvocationHandler<P> implements InvocationHandler {

    private final static Pattern getterPattern = Pattern.compile("(?:get|is)([\\w$]+)");

    protected abstract Object getPropertyValue(String property);

    protected abstract <R> R createProxyObject(Class<R> returnType, String property);

    protected abstract <E> List<E> createProxyList(final Class<E> componentType, final String property);

    protected <R, E> R convertToReturnType(Class<R> returnType, Class<E> componentType, List list) {
        if (list == null) {
            return null;
        } else if (returnType.isArray()) {
            Object newArray = Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(newArray, i, list.get(i));
            }
            return returnType.cast(newArray);
        } else if (List.class.isAssignableFrom(returnType)) {
            return returnType.cast(list);
        } else if (Set.class.isAssignableFrom(returnType)) {
            return returnType.cast(new HashSet<>(list));
        } else {
            throw new IllegalStateException("unknown collection type: " + returnType.getSimpleName());
        }
    }

    private <R, E> R handleCollectionGetter(Class<R> returnType, Class<E> componentType, String property) {
        ConvertUtils.deregister(returnType);
        ConvertUtils.register(new CollectionConverter(componentType), returnType);
        return returnType.cast(ConvertUtils.convert(getPropertyValue(property), returnType));
    }

    protected <R> R handleGetter(Class<R> returnType, Type genericReturnType, String property) {
        if (Iterable.class.isAssignableFrom(returnType)) {
            final Class<?> genericType = returnType.isArray() ? returnType.getComponentType() : ResultHelper.getGenericType(genericReturnType);
            if (genericType.isInterface()) {
                return convertToReturnType(returnType, genericType, createProxyList(genericType, property));
            } else {
                return handleCollectionGetter(returnType, genericType, property);
            }
        } else if (returnType.isInterface()) {
            return createProxyObject(returnType, property);
        } else {
            return (R) ConvertUtils.convert(getPropertyValue(property), returnType);
        }
    }

    protected String handleToString() {
        return "";
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName())) {
            return handleToString();
        } else {
            Matcher m = getterPattern.matcher(method.getName());
            if (m.matches()) {
                return handleGetter(method.getReturnType(), method.getGenericReturnType(), StringUtils.uncapitalize(m.group(1)));
            }
        }
        throw new UnsupportedOperationException("method: " + method.toGenericString() + " is not supported!");
    }

    static {
        ConvertUtils.deregister(Date.class);
        final DateConverter conv = new DateConverter(null);
        conv.setPattern("yyyy-MM-dd");
        conv.setLocale(Locale.getDefault());
        conv.setUseLocaleFormat(true);
        ConvertUtils.register(conv, Date.class);
        ConvertUtils.register(new ArrayConverter(Object[].class, conv), Date[].class);
    }
}
