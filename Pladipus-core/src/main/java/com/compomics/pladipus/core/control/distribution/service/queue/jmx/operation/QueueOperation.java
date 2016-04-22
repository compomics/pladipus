package com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation;

import com.compomics.pladipus.core.control.distribution.service.queue.jmx.JMXQueueConnectionCache;
import com.sun.mail.iap.ConnectionException;
import java.io.IOException;

/**
 *
 * @author Kenneth Verheggen
 */
public abstract class QueueOperation {

    /**
     * A cache for the connections to the Pladipus Queues
     */
    protected final JMXQueueConnectionCache cache;

    public QueueOperation() throws IOException, ConnectionException {
        cache = JMXQueueConnectionCache.getInstance();
    }

    public JMXQueueConnectionCache getCache() {
        return cache;
    }

}
