package de.herschke.neo4j.uplink.ejb.calls;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import de.herschke.neo4j.uplink.api.Neo4jServerException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.json.simple.JSONValue;

/**
 *
 * @author rhk
 */
public abstract class AbstractNeo4jServerCall<Res> implements Neo4jServerCall<Res> {

    private final EnumSet<ClientResponse.Status> expectedStatus;

    protected AbstractNeo4jServerCall() {
        this(EnumSet.of(ClientResponse.Status.OK));
    }

    protected AbstractNeo4jServerCall(ClientResponse.Status expectedStatus) {
        this.expectedStatus = EnumSet.of(expectedStatus);
    }

    protected AbstractNeo4jServerCall(EnumSet<ClientResponse.Status> expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    /**
     * returns the method to be used when calling the server.
     * <p>
     * the default value is
     * <code>POST<code>
     *
     * @return the method
     */
    protected String getMethod() {
        return "POST";
    }

    /**
     * returns the path where to execute the call.
     *
     * @return
     */
    protected abstract String getPath();

    /**
     * parses the response.
     *
     * @param status the status code of the response
     * @param responseContent the content of the response as an {@link Reader}
     * @return the result of type {@literal R}
     */
    protected abstract Res parseResponse(ClientResponse.Status status, Reader responseContent) throws Neo4jServerException;

    /**
     * returns the entity to be used for the request or null if no entity has to
     * be used.
     *
     * @return an instance of &lt;String&gt; or null
     */
    protected abstract String getRequestEntity() throws Neo4jServerException;

    /**
     * returns the additional queryParams for this call.
     *
     * @return
     */
    protected Map<String, Object> getQueryParams() {
        return Collections.emptyMap();
    }

    @Override
    public Res execute(WebResource serverResource) throws Neo4jServerException {
        WebResource resource = serverResource.path(getPath());
        for (Map.Entry<String, Object> entry : getQueryParams().entrySet()) {
            resource = resource.queryParam(entry.getKey(), JSONValue.toJSONString(entry.getValue()));
        }
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).method(getMethod(), ClientResponse.class, getRequestEntity());
        if (this.expectedStatus.contains(response.getClientResponseStatus())) {
            return parseResponse(response.getClientResponseStatus(), new LoggingReader(new InputStreamReader(response.getEntityInputStream(), ENCODING)));
        } else if (response.getClientResponseStatus() == ClientResponse.Status.BAD_REQUEST) {
            throw Neo4jServerResponseException.fromServerResponse(new LoggingReader(new InputStreamReader(response.getEntityInputStream(), ENCODING)));
        } else {
            throw new Neo4jServerException(String.format("call to Neo4j Server result in response with status: %s reason: %s%n%s%n", response.getClientResponseStatus(), response.getClientResponseStatus().getReasonPhrase(), response));
        }
    }
}
