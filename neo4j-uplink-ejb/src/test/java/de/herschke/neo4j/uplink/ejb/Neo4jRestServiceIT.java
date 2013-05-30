package de.herschke.neo4j.uplink.ejb;

import de.herschke.neo4j.uplink.api.CypherResult;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import de.herschke.neo4j.uplink.api.Neo4jUplink;
import de.herschke.neo4j.uplink.api.Node;
import de.herschke.neo4j.uplink.api.Relationship;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import static org.fest.assertions.Assertions.assertThat;
import org.fest.assertions.MapAssert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * tests the {@link Neo4jRestService}.
 *
 * @author rhk
 */
@RunWith(Arquillian.class)
public class Neo4jRestServiceIT {

    public static class Movie {

        private String title;
        private String year;

        public Movie(String title, String year) {
            this.title = title;
            this.year = year;
        }

        public String getTitle() {
            return title;
        }

        public String getYear() {
            return year;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setYear(String year) {
            this.year = year;
        }
    }
    @EJB
    Neo4jUplink uplink;

    @Deployment(order = 2, name = "test-candidate")
    public static WebArchive createTestArchive() {
        WebArchive wa = ShrinkWrap.create(WebArchive.class, "sample.war");
        wa.addClasses(Neo4jRestService.class);
        wa.addAsWebInfResource("META-INF/beans.xml");
        wa.addAsWebInfResource("ejb-jar.xml");
        for (File libFile : new File("target/libs").listFiles()) {
            wa.addAsLibrary(libFile, libFile.getName());
        }
        System.out.println("------------------------------- sample.war --------------------------------");
        System.out.println(wa.toString(true));
        System.out.println("---------------------------------------------------------------------------");
        return wa;
    }

    private CypherResult executeCypherQuery(String query) throws Exception {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(query);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        CypherResult result = uplink.executeCypherQuery(query);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.out.println(result);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return result;
    }

    private CypherResult executeCypherQuery(String query, Map<String, Object> params) throws Exception {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(query);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        CypherResult result = uplink.executeCypherQuery(query, params);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.out.println(result);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return result;
    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void simpleQueryTest() throws Exception {
        CypherResult result = executeCypherQuery("START n=node(0) RETURN count(n)", Collections.<String, Object>emptyMap());
        assertThat(result).isNotNull();
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValue(0, "count(n)")).isNotNull().isEqualTo(1L);
    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void noResultTest() throws Exception {
        CypherResult result = executeCypherQuery("START n=node(*) WHERE HAS(n.xyz) RETURN n", Collections.<String, Object>emptyMap());
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void createRelationTest() throws Exception {
        CypherResult result = executeCypherQuery(""
                + "START"
                + "  n=node(*), m=node(*) "
                + "WHERE"
                + "  HAS(n.name) AND n.name = \"Keanu Reeves\""
                + "  AND HAS(m.title) AND m.title = \"The Matrix\""
                + "CREATE"
                + "  (n)<-[:SAVED_LIVE_OF]-(m) "
                + "RETURN n.name AS `name`, m.title AS `title`", Collections.<String, Object>emptyMap());
        assertThat(result).isNotEmpty();
    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void cypherErrorTest() throws Exception {
        try {
            executeCypherQuery("START n=node(*) WHERE n.name!= \"Keanu Reeves\" RETURN n.name", Collections.<String, Object>emptyMap());
            fail("must throw Exception");
        } catch (Exception exception) {
            assertThat(exception).isInstanceOf(Neo4jServerException.class);
            assertThat(exception.getMessage()).startsWith("SyntaxException");
        }
    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void propertiesMustExistTest() throws Exception {
        CypherResult result = null;
        result = executeCypherQuery("START n=node(*) WHERE n.name! =\"Keanu Reeves\" RETURN n.name", Collections.<String, Object>emptyMap());
        assertThat(result).isNotNull();
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValue(0, "n.name")).isNotNull().isEqualTo("Keanu Reeves");
        result = executeCypherQuery("START n=node(*) WHERE HAS(n.name) AND n.name=\"Keanu Reeves\" RETURN n.name", Collections.<String, Object>emptyMap());
        assertThat(result).isNotNull();
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValue(0, "n.name")).isNotNull().isEqualTo("Keanu Reeves");
    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void nodeQueryTest() throws Exception {
        CypherResult result = executeCypherQuery("start n=node:node_auto_index(name=\"Keanu Reeves\") return n", Collections.<String, Object>emptyMap());
        assertThat(result).isNotNull();
        assertThat(result.getRowCount()).isEqualTo(1);
        Object cell = result.getValue(0, "n");

        assertThat(cell).isNotNull().isInstanceOf(Node.class);
        Node node = (Node) cell;

        assertThat(node.getPropertyValue("name")).isInstanceOf(String.class).isEqualTo("Keanu Reeves");
    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void queryWithObjectParameterTest() throws Exception {
        Movie newMovie = new Movie("Star Trek - Into Darkness", "2013-05-09");
        CypherResult result = executeCypherQuery("create (n {newMovie}) return n", Collections.<String, Object>singletonMap("newMovie", newMovie));
        assertThat(result).isNotNull();
        assertThat(result.getRowCount()).isEqualTo(1);
        Object cell = result.getValue(0, "n");

        assertThat(cell).isNotNull().isInstanceOf(Node.class);
        Node node = (Node) cell;

        assertThat(node.getPropertyValue("title")).isInstanceOf(String.class).isEqualTo(newMovie.getTitle());
        assertThat(node.getPropertyValue("year")).isInstanceOf(String.class).isEqualTo(newMovie.getYear());

        newMovie.setYear("2013-09-05");
        result = executeCypherQuery("start n=node(" + node.getId() + ") set n.year = {`newMovie.year`} return n.year", Collections.<String, Object>singletonMap("newMovie", newMovie));
        assertThat(result).isNotNull();
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValue(0, "n.year")).isInstanceOf(String.class).isEqualTo(newMovie.getYear());
    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void relationshipQueryTest() throws Exception {
        CypherResult result = executeCypherQuery("start n=node:node_auto_index(name=\"Keanu Reeves\") match n-[r:ACTS_IN]->m return r, ID(n), ID(m)", Collections.<String, Object>emptyMap());

        assertThat(result).isNotNull();
        assertThat(result.getColumnCount()).isEqualTo(3);
        assertThat(result.getRowCount()).isEqualTo(3);
        int n = ((Long) result.getValue(0, "ID(n)")).intValue();
        int m = ((Long) result.getValue(0, "ID(m)")).intValue();
        Object cell = result.getValue(0, "r");

        assertThat(cell).isNotNull().isInstanceOf(Relationship.class);
        Relationship rel = (Relationship) cell;

        assertThat(rel.getType()).isEqualTo("ACTS_IN");
        assertThat(rel.getPropertyValue("role")).isInstanceOf(String.class).isEqualTo("Neo");
        assertThat(rel.getStartId()).isEqualTo(n);
        assertThat(rel.getEndId()).isEqualTo(m);

    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void multipleColumnsQueryTest() throws Exception {
        CypherResult result = executeCypherQuery("start n=node:node_auto_index(name={actorname}) \n"
                + "match n-[r:ACTS_IN]->m \n"
                + "return n.name as `actor.name`, m.title as `movie.title`, m.year as `movie.year`, r.role as `role.name`", Collections.<String, Object>singletonMap("actorname", "Keanu Reeves"));

        assertThat(result)
                .isNotNull();
        assertThat(result.getRowCount()).isEqualTo(3);
        Map<String, Object> row = result.getRowData(1);

        assertThat(row)
                .isNotNull();
        assertThat(row.keySet()).doesNotHaveDuplicates().containsOnly("actor.name", "movie.title", "movie.year", "role.name");
        assertThat(row)
                .includes(
                MapAssert.entry("actor.name", "Keanu Reeves"),
                MapAssert.entry("movie.title", "The Matrix Reloaded"),
                MapAssert.entry("movie.year", "2003-05-07"),
                MapAssert.entry("role.name", "Neo"));

    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void aggregateQueryTest() throws Exception {
        CypherResult result = executeCypherQuery("start n=node:node_auto_index(name={actorname}) "
                + "match n-[ACTS_IN]->m \n"
                + "return n.name as `actor.name`, collect(m.title) as `movies.title`", Collections.<String, Object>singletonMap("actorname", "Keanu Reeves"));

        assertThat(result)
                .isNotNull();
        assertThat(result.getRowCount()).isEqualTo(1);
        Map<String, Object> row = result.getRowData(0);

        assertThat(row)
                .isNotNull().includes(MapAssert.entry("actor.name", "Keanu Reeves"));
        assertThat(row.containsKey("movies.title"));
        assertThat(row.get("movies.title")).isNotNull().isInstanceOf(List.class);
        List aggregateResult = (List) row.get("movies.title");

        assertThat(aggregateResult)
                .hasSize(3).doesNotHaveDuplicates().containsOnly("The Matrix", "The Matrix Reloaded", "The Matrix Revolutions");
    }

    @Test
    @OperateOnDeployment("test-candidate")
    public void labelsTest() throws Exception {
        CypherResult result = executeCypherQuery("start n=node(*) where HAS(n.name) return ID(n)", Collections.<String, Object>emptyMap());

        int id = -1;
        for (Object o : result.getColumnValues(0)) {
            id = ((Long) o).intValue();
            String[] labelsBeforeAdd = uplink.getNodeLabels(id);
            assertThat(labelsBeforeAdd).isEmpty();

            assertThat(uplink.addNodeLabel(id, "actor")).isTrue();
            String[] labelsAfterSingleAdd = uplink.getNodeLabels(id);
            assertThat(labelsAfterSingleAdd).hasSize(1).containsOnly("actor");
        }
        assertThat(id).isGreaterThanOrEqualTo(0);

        assertThat(uplink.addNodeLabel(id, "favorite_actor", "matrix_actor")).isTrue();

        String[] labelsAfterMultiAdd = uplink.getNodeLabels(id);
        assertThat(labelsAfterMultiAdd).hasSize(3).containsOnly("actor", "matrix_actor", "favorite_actor");

        Node[] nodes = uplink.getNodesWithLabelAndProperties("matrix_actor", Collections.<String, Object>singletonMap("name", "Keanu Reeves"));
        assertThat(nodes).hasSize(1);
        assertThat(nodes[0].getPropertyValue("name")).isEqualTo("Keanu Reeves");
        assertThat(nodes[0].getId()).isEqualTo(id);

        nodes = uplink.getNodesWithLabel("actor");
        assertThat(nodes).hasSize(result.getRowCount());

        result = executeCypherQuery("START n=node(*) WHERE n:actor RETURN n");
        assertThat(result.getRowCount()).isEqualTo(nodes.length);

        result = executeCypherQuery("MATCH n:matrix_actor:favorite_actor:actor RETURN n");
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getValue(0, "n")).isInstanceOf(Node.class);
        assertThat(((Node) result.getValue(0, "n")).getId()).isEqualTo(id);
    }
}
