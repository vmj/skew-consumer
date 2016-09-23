package fi.linuxbox.skew.consumer;

import java.util.concurrent.*;
import javax.enterprise.inject.*;
import org.slf4j.*;

import static java.util.concurrent.TimeUnit.*;

/**
 *
 */
public class ExecutorServiceProducer {
    private final Logger log = LoggerFactory.getLogger(ExecutorServiceProducer.class);

    @Produces
    public ExecutorService get() {
        return Executors.newSingleThreadExecutor();
    }

    public void close(@Disposes ExecutorService pool) {
        log.debug("shutting down the thread pool now");
        pool.shutdownNow();
        try {
            log.debug("awaiting termination of consumer");
            if (!pool.awaitTermination(15, SECONDS)) {
                log.error("pool did not terminate");
            }

        } catch (final InterruptedException e) {
            log.info("interrupted; shutting down again");
            pool.shutdownNow();
            log.debug("preserving interrupt status");
            Thread.currentThread().interrupt();
        }

        log.debug("complete");
    }
}
