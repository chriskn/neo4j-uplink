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
package de.herschke.neo4j.uplink.ejb.response.handling;

import de.herschke.neo4j.uplink.api.CypherResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.fest.assertions.Assertions.assertThat;
import org.fest.assertions.MapAssert;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

/**
 *
 * @author rhk
 */
public class CypherResponseHandlerTest {

    @Test
    public void testEmptyResponse() throws Exception {
        String testJson = "{\n"
                + "  \"columns\" : [ ],\n"
                + "  \"data\" : [ ]\n"
                + "}\n";
        CypherResult result = parseJson(testJson);
        assertThat(result).isEmpty();
    }

    @Test
    public void testSinglePrimitiveColumn() throws Exception {
        String testJson = "{\n"
                + "    \"columns\": [\"test\"],\n"
                + "    \"data\": [ [ \"blabla\" ]]\n"
                + "}\n";

        CypherResult result = parseJson(testJson);

        assertThat(result.getColumnCount()).isEqualTo(1);
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValue(0, "test")).isNotNull().isInstanceOf(String.class).isEqualTo("blabla");

    }

    @Test
    public void testMultipleColumn() throws Exception {
        String testJson = "{\n"
                + "    \"columns\": [\"test1\", \"test2\"],\n"
                + "    \"data\": [[ [9,8,7], [5,3,2] ], [\"abc\", [7,6,5]]]\n"
                + "}\n";
        CypherResult result = parseJson(testJson);

        assertThat(result.getColumnCount()).isEqualTo(2);
        assertThat(result.getColumnNames()).containsExactly("test1", "test2");

        assertThat(result.getRowCount()).isEqualTo(2);

        assertThat(result.getValue(0, "test1")).isNotNull().isInstanceOf(List.class);
        assertThat((List) result.getValue(0, "test1")).containsExactly(9L, 8L, 7L);
        assertThat(result.getValue(0, "test2")).isNotNull().isInstanceOf(List.class);
        assertThat((List) result.getValue(0, "test2")).containsExactly(5L, 3L, 2L);

        assertThat(result.getValue(1, "test1")).isNotNull().isInstanceOf(String.class).isEqualTo("abc");
        assertThat(result.getValue(1, "test2")).isNotNull().isInstanceOf(List.class);
        assertThat((List) result.getValue(1, "test2")).containsExactly(7L, 6L, 5L);

    }

    @Test
    public void testSpecial() throws Exception {
        String testJson = "{\n"
                + "  \"columns\" : [ \"r\", \"ID(n)\", \"ID(m)\" ],\n"
                + "  \"data\" : [ [ {\n"
                + "    \"start\" : \"http://localhost:7474/db/data/node/3\",\n"
                + "    \"data\" : {\n"
                + "      \"role\" : \"Neo\"\n"
                + "    },\n"
                + "    \"property\" : \"http://localhost:7474/db/data/relationship/0/properties/{key}\",\n"
                + "    \"self\" : \"http://localhost:7474/db/data/relationship/0\",\n"
                + "    \"properties\" : \"http://localhost:7474/db/data/relationship/0/properties\",\n"
                + "    \"type\" : \"ACTS_IN\",\n"
                + "    \"extensions\" : {\n"
                + "    },\n"
                + "    \"end\" : \"http://localhost:7474/db/data/node/6\"\n"
                + "  }, 3, 6 ], [ {\n"
                + "    \"start\" : \"http://localhost:7474/db/data/node/3\",\n"
                + "    \"data\" : {\n"
                + "      \"role\" : \"Neo\"\n"
                + "    },\n"
                + "    \"property\" : \"http://localhost:7474/db/data/relationship/1/properties/{key}\",\n"
                + "    \"self\" : \"http://localhost:7474/db/data/relationship/1\",\n"
                + "    \"properties\" : \"http://localhost:7474/db/data/relationship/1/properties\",\n"
                + "    \"type\" : \"ACTS_IN\",\n"
                + "    \"extensions\" : {\n"
                + "    },\n"
                + "    \"end\" : \"http://localhost:7474/db/data/node/5\"\n"
                + "  }, 3, 5 ], [ {\n"
                + "    \"start\" : \"http://localhost:7474/db/data/node/3\",\n"
                + "    \"data\" : {\n"
                + "      \"role\" : \"Neo\"\n"
                + "    },\n"
                + "    \"property\" : \"http://localhost:7474/db/data/relationship/2/properties/{key}\",\n"
                + "    \"self\" : \"http://localhost:7474/db/data/relationship/2\",\n"
                + "    \"properties\" : \"http://localhost:7474/db/data/relationship/2/properties\",\n"
                + "    \"type\" : \"ACTS_IN\",\n"
                + "    \"extensions\" : {\n"
                + "    },\n"
                + "    \"end\" : \"http://localhost:7474/db/data/node/4\"\n"
                + "  }, 3, 4 ] ]\n"
                + "}\n";
        CypherResult result = parseJson(testJson);

        assertThat(result.getColumnCount()).isEqualTo(3);
        assertThat(result.getRowCount()).isEqualTo(3);
    }

    @Test
    public void testObjectColumn() throws Exception {
        String testJson = "{\n"
                + "    \"columns\": [\"test\", \"bla\"],\n"
                + "    \"data\": [ [ {\"blabla\" : 12, \"xyz\" : [5,3,1]}, 3] ]\n"
                + "}\n";

        CypherResult result = parseJson(testJson);

        assertThat(result.getColumnCount()).isEqualTo(2);
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValue(0, "test")).isNotNull().isInstanceOf(Map.class);
        assertThat((Map) result.getValue(0, 0)).isNotEmpty().includes(MapAssert.entry("blabla", 12L));
        assertThat(((Map) result.getValue(0, 0)).get("xyz")).isInstanceOf(List.class);
        assertThat((List) ((Map) result.getValue(0, 0)).get("xyz")).containsExactly(5L, 3L, 1L);
        assertThat(result.getValue(0, "bla")).isEqualTo(3L);

    }

    private CypherResult parseJson(String testJson) throws ParseException {
        System.out.println(">>>>>>>>>>> json: >>>>>>>>>>");
        System.out.println(testJson);
        final CypherResponseHandler handler = new CypherResponseHandler("",
                Collections.<String, Object>emptyMap());
        JSONParser parser = new JSONParser();
        parser.parse(testJson, handler);
        CypherResult result = handler.getResult();
        System.out.println("<<<<<<<<<< result: <<<<<<<<<");
        System.out.println(result);
        return result;
    }
}
