package fi.linuxbox.skew.consumer.app;

import org.apache.webbeans.config.*;
import org.apache.webbeans.spi.*;
import org.slf4j.*;

/**
 *
 */
public class Boot
{
    public static void main(final String[] args) throws InterruptedException
    {
        final ContainerLifecycle containerLifecycle = WebBeansContext.currentInstance()
                                                                     .getService(ContainerLifecycle.class);

        containerLifecycle.startApplication(null);

        Runtime.getRuntime().addShutdownHook(new Cleanup(containerLifecycle));

        final Object o = new Object();
        synchronized (o) { o.wait(); }
    }

    private static class Cleanup extends Thread {
        private final Logger log = LoggerFactory.getLogger(Cleanup.class);
        final ContainerLifecycle containerLifecycle;

        public Cleanup(final ContainerLifecycle containerLifecycle)
        {
            this.containerLifecycle = containerLifecycle;
        }

        @Override
        public void run()
        {
            log.debug("stopping application");
            containerLifecycle.stopApplication(null);
        }
    }
}
