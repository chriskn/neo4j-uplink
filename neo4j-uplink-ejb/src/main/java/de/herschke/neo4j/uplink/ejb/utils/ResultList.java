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

import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.ejb.response.conversion.CypherResultInvocationHandler;
import java.lang.reflect.Proxy;
import java.util.AbstractList;

/**
 *
 * @author rhk
 */
public class ResultList<T> extends AbstractList<T> {
    private final CypherResult result;
    private final Class<T> type;
    private final int size;
    private final T[] proxies;

    public ResultList(CypherResult result, Class<T> type) {
        this.result = result;
        this.type = type;
        this.size = result.getRowCount();
        this.proxies = (T[]) new Object[size];
    }

    @Override
    public T get(int index) {
        if (index >= size()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (proxies[index] == null) {
            proxies[index] = (T) Proxy.newProxyInstance(
                    type.getClassLoader(),
                    new Class[]{type,
                CypherResult.class},
                    new CypherResultInvocationHandler("",
                    result,
                    index));
        }
        return proxies[index];
    }

    @Override
    public int size() {
        return size;
    }
}
