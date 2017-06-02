package fi.linuxbox.skew.consumer;

import javax.annotation.*;
import javax.inject.*;

import fi.linuxbox.skew.consumer.annotation.Requests;
import fi.linuxbox.skew.consumer.annotation.Responses;
import org.apache.camel.*;
import org.slf4j.*;

/**
 *
 */
public class Consumer implements Runnable {
    private final Logger log = LoggerFactory.getLogger(Consumer.class);

    @Inject @Requests private Endpoint requests;
    private PollingConsumer requestConsumer;

    @Inject private ProducerTemplate responseProducer;
    @Inject @Responses private Endpoint responses;

    private boolean stopping = false;

    @PostConstruct
    private void init() {
        try {
            requestConsumer = requests.createPollingConsumer();
            requestConsumer.start();
        } catch (Exception e) {
            throw new RuntimeException("unable to create polling consumer", e);
        }
    }

    /**
     * Camel CDI event listener.
     * <p>
     * The only purpose is to prevent the error in the log when .receive() gets interrupted.
     * </p>
     */
    @PreDestroy
    private void stop() {
        stopping = true;
        if (requestConsumer != null) {
            try {
                requestConsumer.stop();
            } catch (final Exception e) {
                log.error("stop request consumer exception: " + e.getMessage(), e);
            }
        }
        try {
            responseProducer.stop();
        } catch (final Exception e) {
            log.error("stop response producer exception: " + e.getMessage(), e);
        }
    }

    /**
     * The main loop.
     * <p>
     * This is run in a separate thread.
     * </p>
     */
    @Override
    public void run() {
        try {
            for (;;) {
                Exchange exchange = requestConsumer.receive();// blocks

                if (stopping && exchange == null) {
                    log.info("returning from run");
                    break;
                }

                log.info("handling exchange: " + exchange);

                Thread.sleep(5000);

                log.info("exchange done.");

                responseProducer.asyncSend(responses, exchange);
            }

        } catch (final Throwable e) {
            if (!stopping)
                log.error("run exception: " + e.getMessage(), e);
        }
    }
}
