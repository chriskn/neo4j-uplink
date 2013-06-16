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

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.StringConverter;

/**
 *
 * @author rhk
 */
public class CollectionConverter extends ArrayConverter {

    private final Class<?> genericType;

    public CollectionConverter(Class<?> genericType) {
        super(Object[].class, new StringConverter());
        this.genericType = genericType;
    }

    private Object convertToEnum(Object element) {
        for (Object o : this.genericType.getEnumConstants()) {
            if (((Enum) o).name().equalsIgnoreCase(element.toString())) {
                return o;
            }
        }
        return element;
    }

    @Override
    protected Object convertToType(Class type, Object value) throws Throwable {
        // Handle the source
        int size = 0;
        Iterator iterator = null;
        if (value.getClass().isArray()) {
            size = Array.getLength(value);
        } else {
            Collection collection = convertToCollection(type, value);
            size = collection.size();
            iterator = collection.iterator();
        }

        final Object newArray = Array.newInstance(this.genericType, size);

        // Convert and set each element in the new Array
        for (int i = 0; i < size; i++) {
            Object element = iterator == null ? Array.get(value, i) : iterator
                    .next();
            // TODO - probably should catch conversion errors and throw
            //        new exception providing better info back to the user
            if (this.genericType.isEnum()) {
                element = convertToEnum(element);
            } else {
                element = ConvertUtils.convert(element, this.genericType);
            }
            Array.set(newArray, i, element);
        }

        if (type.isArray()) {
            return newArray;
        } else if (List.class.isAssignableFrom(type)) {
            return new AbstractList() {
                @Override
                public Object get(int index) {
                    return Array.get(newArray, index);
                }

                @Override
                public int size() {
                    return Array.getLength(newArray);
                }
            };
        } else if (Set.class.isAssignableFrom(type)) {
            return new HashSet(new AbstractList() {
                @Override
                public Object get(int index) {
                    return Array.get(newArray, index);
                }

                @Override
                public int size() {
                    return Array.getLength(newArray);
                }
            });
        } else {
            throw new ClassCastException("cannot convert the array to: " + type
                    .getSimpleName());
        }
    }
}
