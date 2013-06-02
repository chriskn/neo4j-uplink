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
package de.herschke.neo4j.uplink.ejb.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

/**
 * collects some helper methods for JSON handling.
 *
 * @author rhk
 */
public final class JSONHelper {

    private static boolean isJSONAware(Object value) {
        return value instanceof String || value instanceof Double || value instanceof Float || value instanceof Number || value instanceof Boolean || value instanceof JSONStreamAware || value instanceof JSONAware || value instanceof Map || value instanceof List;
    }

    private JSONHelper() {
        // don't allow instantiation.
    }

    public static JSONObject toDeepJSONObject(Map<String, Object> map) {
        JSONObject object = new JSONObject();
        for (Entry<String, Object> entry : map.entrySet()) {
            addToJSONObject(object, entry.getKey(), entry.getValue());
        }
        return object;
    }

    private static void addToJSONObject(JSONObject object, String prefix, Object value) {
        if (isJSONAware(value)) {
            object.put(prefix, value);
        } else if (value instanceof Class) {
            object.put(prefix, ((Class) value).getSimpleName());
        } else if (value instanceof Date) {
            object.put(prefix, Long.valueOf(((Date) value).getTime()));
        } else if (value != null) {
            try {
                JSONObject innerObject = new JSONObject();
                for (Method method : value.getClass().getMethods()) {
                    if (!"getClass".equals(method.getName()) && (method.getName().startsWith("get") || method.getName().startsWith("is")) && method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                        String name = method.getName().startsWith("get") ? (method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4)) : (method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3));
                        final Object newValue = method.invoke(value);
                        addToJSONObject(object, prefix + "." + name, newValue);
                        if (isJSONAware(newValue)) {
                            innerObject.put(name, newValue);
                        } else if (newValue instanceof Class) {
                            innerObject.put(name, ((Class) newValue).getSimpleName());
                        } else if (newValue instanceof Date) {
                            innerObject.put(name, Long.valueOf(((Date) newValue).getTime()));
                        } else if (newValue != null) {
                            innerObject.put(name, newValue.toString());
                        }
                    }
                }
                object.put(prefix, innerObject);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new IllegalArgumentException(String.format("cannot build JSON from Object, due to: %s(%s)", ex.getClass().getSimpleName(), ex.getMessage()), ex);
            }
        }
    }
}
