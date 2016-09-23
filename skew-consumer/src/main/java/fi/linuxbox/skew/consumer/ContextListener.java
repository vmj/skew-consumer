package fi.linuxbox.skew.consumer;

import java.util.concurrent.*;
import javax.enterprise.context.*;
import javax.enterprise.event.*;
import javax.inject.*;
import org.apache.camel.management.event.*;
import org.slf4j.*;

/**
 *
 */
@ApplicationScoped
public class ContextListener {
    private final Logger log = LoggerFactory.getLogger(ContextListener.class);

    @Inject private ExecutorService    pool;
    @Inject private Consumer consumer;

    public void start(@Observes CamelContextStartedEvent ignored) {
        pool.execute(consumer);
    }
}
