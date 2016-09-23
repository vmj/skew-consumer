package fi.linuxbox.skew.consumer;

import fi.linuxbox.skew.consumer.exception.*;

/**
 *
 */
public class Config {
    public String getRequestsEndpointUri() {
        return env("CONSUMER_REQUESTS_ENDPOINT_URI");
    }

    public String getResponsesEndpointUri() {
        return env("CONSUMER_RESPONSES_ENDPOINT_URI");
    }

    private String env(final String var) {
        final String val = System.getenv(var);
        if (val == null || val.isEmpty()) {
            throw new ConfigException("environment variable " + var + " must be specified and not empty");
        }
        return val;
    }
}
