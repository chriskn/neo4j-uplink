package de.herschke.neo4j.uplink.ejb.calls;

import com.sun.jersey.api.client.WebResource;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import java.nio.charset.Charset;

/**
 * a call to the Neo4j CommunityServer
 *
 * @author rhk
 */
public interface Neo4jServerCall<R> {
    static final Charset ENCODING = Charset.forName("UTF-8");

    /**
     * calls the Neo4j CommunityServer (based on the given serverResource)
     *
     * @param serverResource a {@link WebResource} that represents the Neo4j
     * CommunityServer
     * @return the result
     * @throws Neo4jServerException if something went wrong on the server
     */
    R execute(WebResource serverResource) throws Neo4jServerException;
}
