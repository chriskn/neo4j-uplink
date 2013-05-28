/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.herschke.neo4j.uplink.ejb.responsehandling;

import de.herschke.neo4j.uplink.api.CypherResult;
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
        final CypherResponseHandler handler = new CypherResponseHandler();
        JSONParser parser = new JSONParser();
        parser.parse(testJson, handler);
        CypherResult result = handler.getResult();
        System.out.println("<<<<<<<<<< result: <<<<<<<<<");
        System.out.println(result);
        return result;
    }
}
