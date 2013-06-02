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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rhk
 */
public final class ResultHelper {

    private ResultHelper() {
    }

    public static Object extractValue(Object value, int index) {
        if (value instanceof List) {
            return ((List) value).get(index);
        } else if (value instanceof Object[]) {
            return ((Object[]) value)[index];
        } else if (index == 0) {
            return value;
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public static List<Map<String, Object>> invert(Map<String, Object> map, String prefix) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                String key = entry.getKey().substring(prefix.length());
                if (Iterable.class.isAssignableFrom(entry.getValue().getClass())) {
                    int i = 0;
                    for (Object e : ((Iterable) entry.getValue())) {
                        for (int j = list.size(); j <= i; j++) {
                            list.add(new HashMap<String, Object>());
                        }
                        list.get(i).put(key, e);
                        i++;
                    }
                } else {
                    if (list.isEmpty()) {
                        list.add(new HashMap<String, Object>());
                    }
                    list.get(0).put(key, entry.getValue());
                }
            }
        }
        return list;
    }

    public static boolean isAssignableOrIterableOfType(Class<?> toTest, Type genericTestType, Class<?> expected) {
        return expected.isAssignableFrom(toTest) || isIterableOfType(toTest, genericTestType, expected);
    }

    public static boolean isIterableOfType(Class<?> toTest, Type genericTestType, Class<?> expected) {
        if (toTest.isArray() && expected.isAssignableFrom(toTest.getComponentType())) {
            return true;
        } else if (Collection.class.isAssignableFrom(toTest) && expected.isAssignableFrom(getGenericType(genericTestType))) {
            return true;
        }
        return false;
    }

    public static Class<?> getGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pgrt = (ParameterizedType) type;
            return (Class<?>) pgrt.getActualTypeArguments()[0];
        } else {
            throw new ClassCastException(String.format("the genericReturnType: %s is not a ParameterizedType!", type == null ? "null" : type.toString()));
        }
    }
}
