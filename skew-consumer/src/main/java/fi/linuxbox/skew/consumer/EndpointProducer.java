package fi.linuxbox.skew.consumer;

import fi.linuxbox.skew.consumer.annotation.*;

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
}
