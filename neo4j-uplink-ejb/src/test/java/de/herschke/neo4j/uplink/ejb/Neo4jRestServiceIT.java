package de.herschke.neo4j.uplink.ejb;

import de.herschke.neo4j.uplink.api.CypherResult;
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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * tests the {@link Neo4jRestService}.
 *
 * @author rhk
 */
@RunWith(Arquillian.class)
public class Neo4jRestServiceIT {

    @EJB
    Neo4jUplink qe;

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

    private CypherResult executeCypherQuery(String query, Map<String, Object> params) throws Exception {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(query);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        CypherResult result = qe.executeCypherQuery(query, params);
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
    public void cypherErrorTest() throws Exception {
        try {
            executeCypherQuery("START n=node(*) WHERE n.name!=\"Keanu Reeves\" RETURN n.name", Collections.<String, Object>emptyMap());
        } catch (Exception exception) {
            assertThat(exception.getMessage()).startsWith("Cypher-Exception: SyntaxException");
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
}
