neo4j-uplink
============

A very simple "uplink" facade to the Neo4j CommunityServer ReST API

## Usage

just add the dependency to the API to your project:

~~~~~
<project>
  ...
  <dependencies>
    ...
    <dependency>
      <groupId>de.herschke</groupId>
      <artifactId>neo4j-uplink-api</artifactId>
      <version>${neo4j.uplink.api.version}</version>
    </dependency>
  </dependencies>
  ...
</project>
~~~~~

This brings support for the interface: `de.herschke.neo4j.uplink.api.Neo4jUplink` as a facade in your projects.

To use it practically, you have to add the `neo4j-uplink-ejb` project as an EJB-Module to your EAR-Module. This is done with the following pom-snippet:

~~~~~
<project>
  ...
  <packaging>ear</packaging>
  ...
  <dependencies>
   ...
    <dependency>
      <groupId>de.herschke</groupId>
      <artifactId>neo4j-uplink-ejb</artifactId>
      <version>${neo4j.uplink.ejb.version}</version>
      <type>ejb</type>
    </dependency>
  </dependencies>
  ...
  <build>
    <plugins>
      ...
  		<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-ear-plugin</artifactId>
				<version>2.8</version>
				<configuration>
					<version>6</version>
					<defaultJavaBundleDir>lib/</defaultJavaBundleDir>
                    <modules>
	                    <jarModule>
		                    <groupId>de.herschke</groupId>
		                    <artifactId>neo4j-uplink-api</artifactId>
	                    </jarModule>
	                    <ejbModule>
		                    <groupId>de.herschke</groupId>
		                    <artifactId>neo4j-uplink-ejb</artifactId>
	                    </ejbModule>
                      ...
                    </modules>
				</configuration>
			</plugin>
      ...
    </plugins>
  </build>
  ...
</project>
~~~~~~

Then you can use the interface by injecting it with @EJB anywhere in your beans:

~~~~~~
@Stateless
public class MyBean {

  @EJB
  Neo4jUplink neo4j;
  
  public void myBusinessMethod() throws Exception {
    String cypher = "START n=node(0) RETURN n";
    CypherResult result = neo4j.executeCypherQuery(cypher, Collections.<String,Object>emptyMap());
    Node node = (Node)result.getValue(0,"n");
    // ... do something with node...
  }
}
~~~~~~
