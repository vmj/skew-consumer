package fi.linuxbox.skew.consumer;

import javax.enterprise.inject.*;
import javax.enterprise.util.*;
import javax.inject.*;
import org.apache.camel.*;
import org.apache.camel.builder.*;

import static org.apache.camel.LoggingLevel.*;

/**
 *
 */
public class TestRoute extends RouteBuilder {
    @Inject private            Instance<Endpoint> endpoints;

    private final boolean enableThisTestRoute = false;

    @Override
    public void configure()
            throws Exception {
        if (!enableThisTestRoute)
            return;

        // Programmatic inject.  I don't want to inject the endpoints if this class isn't even enabled.
        final Endpoint requests = endpoints.select(new AnnotationLiteral<Requests>() {}).get();
        final Endpoint responses = endpoints.select(new AnnotationLiteral<Responses>() {}).get();

        from("timer://jdkTimer?period=2500")
                .log(DEBUG, log, "TiMeR", "New request")
                .to(requests);

        from(responses)
                .log(DEBUG, log, "MaRkEr", "Got response")
                .to("mock:end");
    }
}
