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
import de.herschke.neo4j.uplink.api.Node;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import static org.fest.assertions.Assertions.assertThat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author rhk
 */
@RunWith(value = Parameterized.class)
public class CypherResultInvocationHandlerTest {

    private static final Calendar cal1 = Calendar.getInstance();
    private static final Calendar cal2 = Calendar.getInstance();

    static {
        cal1.set(1977, 4, 7);
        cal2.set(1979, 8, 4);
    }

    @Parameters
    public static Collection<Object[]> data() {
        DummyCypherResult thisResult = new DummyCypherResult("this", "actor", "actors") {
            @Override
            public String toString() {
                return "this-result";
            }
        };
        DummyCypherResult plainResult = new DummyCypherResult("actor.name", "actors.name", "name", "names", "date", "dates", "long", "longs") {
            @Override
            public String toString() {
                return "plain-result";
            }
        };
        JSONObject nodeObject = new JSONObject();
        JSONObject nodeData = new JSONObject();
        nodeObject.put("self", "http://localhost:7474/db/data/node/3");
        nodeObject.put("data", nodeData);

        JSONObject subNodeObject = new JSONObject();
        JSONObject subNodeData = new JSONObject();
        subNodeObject.put("self", "http://localhost:7474/db/data/node/3");
        subNodeObject.put("data", subNodeData);

        JSONObject subNodeObject2 = new JSONObject();
        JSONObject subNodeData2 = new JSONObject();
        subNodeObject2.put("self", "http://localhost:7474/db/data/node/3");
        subNodeObject2.put("data", subNodeData2);

        plainResult.setColumnValues("name", "Robert Herschke");
        plainResult.setColumnValues("actor.name", "Keanu Reeves");
        nodeData.put("name", "Robert Herschke");
        subNodeData.put("name", "Keanu Reeves");
        subNodeData2.put("name", "Laurence Fishburne");

        JSONArray actorNamesArray = new JSONArray();
        actorNamesArray.add("Keanu Reeves");
        actorNamesArray.add("Laurence Fishburne");
        plainResult.setColumnValues("actors.name", actorNamesArray);

        JSONArray namesArray = new JSONArray();
        namesArray.add("Robert Herschke");
        namesArray.add("blabla");
        plainResult.setColumnValues("names", namesArray);
        nodeData.put("names", namesArray);

        plainResult.setColumnValues("date", "1977-05-07");
        nodeData.put("date", "1977-05-07");

        plainResult.setColumnValues("long", cal1.getTime().getTime());
        nodeData.put("long", cal1.getTime().getTime());

        JSONArray datesArray = new JSONArray();
        datesArray.add("1979-09-04");
        datesArray.add("1977-05-07");
        plainResult.setColumnValues("dates", datesArray);
        nodeData.put("dates", datesArray);

        JSONArray longArray = new JSONArray();
        longArray.add(cal2.getTime().getTime());
        longArray.add(cal1.getTime().getTime());
        plainResult.setColumnValues("longs", longArray);
        nodeData.put("longs", longArray);

        thisResult.setColumnValues("this", new Node(nodeObject));
        thisResult.setColumnValues("actor", new Node(subNodeObject));

        JSONArray nodeArray = new JSONArray();
        nodeArray.add(new Node(subNodeObject));
        nodeArray.add(new Node(subNodeObject2));
        thisResult.setColumnValues("actors", nodeArray);

        Object[][] dataArray = new Object[][]{{plainResult}, {thisResult}};
        return Arrays.asList(dataArray);
    }
    public List<Date> dateListField;
    public List<String> stringListField;
    public List<Actor> actorListField;
    private CypherResultInvocationHandler invocationHandler;

    public CypherResultInvocationHandlerTest(CypherResult result) {
        this.invocationHandler = new CypherResultInvocationHandler("", result, 0);
    }

    private Type getTypeOfDateList() {
        try {
            return getClass().getField("dateListField").getGenericType();
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Type getTypeOfStringList() {
        try {
            return getClass().getField("stringListField").getGenericType();
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Type getTypeOfActorList() {
        try {
            return getClass().getField("actorListField").getGenericType();
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testNonExistingNodeProperty() {
        assertThat(invocationHandler.handleGetter(String.class, null, "null")).isNull();
        assertThat(invocationHandler.handleGetter(Date.class, null, "null")).isNull();
    }

    @Test
    public void testStringNodeProperty() {
        assertThat(invocationHandler.handleGetter(String.class, null, "name")).isInstanceOf(String.class).isEqualTo("Robert Herschke");
    }

    @Test
    public void testStringNodePropertyFromArray() {
        assertThat(invocationHandler.handleGetter(String.class, null, "names")).isInstanceOf(String.class).isEqualTo("Robert Herschke");
        assertThat((String[]) invocationHandler.handleGetter(String[].class, null, "names")).containsOnly("Robert Herschke", "blabla");
        assertThat((List) invocationHandler.handleGetter(List.class, getTypeOfStringList(), "names")).containsOnly("Robert Herschke", "blabla");
    }

    @Test
    public void testStringToDateNodeProperty() {
        Object d = invocationHandler.handleGetter(Date.class, null, "date");
        assertThat(d).isNotNull().isInstanceOf(Date.class);
        assertThat(DateUtils.isSameDay(DateUtils.toCalendar((Date) d), cal1)).isTrue();
    }

    @Test
    public void testLongToDateNodeProperty() {
        Object d = invocationHandler.handleGetter(Date.class, null, "long");
        assertThat(d).isNotNull().isInstanceOf(Date.class);
        assertThat(DateUtils.isSameDay(DateUtils.toCalendar((Date) d), cal1)).isTrue();
    }

    @Test
    public void testDateNodePropertyFromStringArray() {
        Object d = invocationHandler.handleGetter(Date.class, null, "dates");
        assertThat(d).isNotNull().isInstanceOf(Date.class);
        assertThat(DateUtils.isSameDay(DateUtils.toCalendar((Date) d), cal2)).isTrue();
        assertThat(DateUtils.isSameDay(DateUtils.toCalendar(((Date[]) invocationHandler.handleGetter(Date[].class, null, "dates"))[0]), cal2)).isTrue();

    }

    @Test
    public void testDateListNodePropertyFromStringArray() {
        Object ds = invocationHandler.handleGetter(List.class, getTypeOfDateList(), "dates");
        assertThat(ds).isInstanceOf(Iterable.class);
        Object d = ((List) ds).get(0);
        assertThat(DateUtils.isSameDay(DateUtils.toCalendar((Date) d), cal2)).isTrue();
    }

    @Test
    public void testDateNodePropertyFromLongArray() {
        Object d = invocationHandler.handleGetter(Date[].class, null, "longs");
        assertThat(d).isNotNull().isInstanceOf(Date[].class);
        assertThat(DateUtils.isSameDay(DateUtils.toCalendar(((Date[]) d)[0]), cal2)).isTrue();
    }

    @Test
    public void testProxyProperty() {
        Object proxy = invocationHandler.handleGetter(Actor.class, null, "actor");
        assertThat(proxy).isInstanceOf(Actor.class);
        assertThat(Proxy.isProxyClass(proxy.getClass())).isTrue();
        assertThat(((Actor) proxy).getName()).isEqualTo("Keanu Reeves");
    }

    @Test
    public void testProxyListProperty() {
        Object proxy = invocationHandler.handleGetter(List.class, getTypeOfActorList(), "actor");
        assertThat(proxy).isInstanceOf(List.class);
        assertThat((List) proxy).hasSize(1);
        final Object element = ((List) proxy).get(0);
        assertThat(element).isNotNull().isInstanceOf(Actor.class);
        assertThat(Proxy.isProxyClass(element.getClass())).isTrue();
        assertThat(((Actor) element).getName()).isEqualTo("Keanu Reeves");
    }

    @Test
    public void testProxyOfListProperty() {
        Object proxy = invocationHandler.handleGetter(Actor.class, null, "actors");
        assertThat(proxy).isInstanceOf(Actor.class);
        assertThat(Proxy.isProxyClass(proxy.getClass())).isTrue();
        assertThat(((Actor) proxy).getName()).isEqualTo("Keanu Reeves");
    }

    @Test
    public void testProxyListOfListProperty() {
        System.out.println(invocationHandler);
        Object proxy = invocationHandler.handleGetter(List.class, getTypeOfActorList(), "actors");
        assertThat(proxy).isInstanceOf(List.class);
        assertThat((List) proxy).hasSize(2);
        Object element = ((List) proxy).get(0);
        assertThat(element).isNotNull().isInstanceOf(Actor.class);
        assertThat(Proxy.isProxyClass(element.getClass())).isTrue();
        assertThat(((Actor) element).getName()).isEqualTo("Keanu Reeves");
        element = ((List) proxy).get(1);
        assertThat(element).isNotNull().isInstanceOf(Actor.class);
        assertThat(Proxy.isProxyClass(element.getClass())).isTrue();
        assertThat(((Actor) element).getName()).isEqualTo("Laurence Fishburne");
    }
}
