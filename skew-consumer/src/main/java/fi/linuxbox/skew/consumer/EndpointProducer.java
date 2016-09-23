package fi.linuxbox.skew.consumer;

import fi.linuxbox.skew.consumer.exception.*;
import javax.enterprise.inject.*;
import javax.inject.*;
import org.apache.camel.*;

/**
 *
 */
public class EndpointProducer {
    @Inject private Config config;
    @Inject private CamelContext camelContext;

    @Produces
    @Requests
    public Endpoint requests() {
        return camelContext.getEndpoint(config.getRequestsEndpointUri());
    }

    @Produces
    @Responses
    public Endpoint responses() {
        return camelContext.getEndpoint(config.getResponsesEndpointUri());
    }

    private Endpoint endpoint(final String uri, final String name) {
        try {
            return camelContext.getEndpoint(uri);
        } catch (final IllegalArgumentException e) {
            throw new ConfigException("endpoint uri " + name + " is invalid: " + uri, e);
        }
    }
}
