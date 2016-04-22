package com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl;

import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.QueueOperation;
import com.compomics.pladipus.core.model.exception.PladipusTrafficException;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.IOException;
import javax.jms.InvalidSelectorException;
import javax.management.MalformedObjectNameException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

/**
 *
 * @author Kenneth Verheggen
 */
public class QueryOperation extends QueueOperation {

    public QueryOperation() throws IOException, ConnectionException {
        super();
    }

    /**
     *
     * @param queue the target queue
     * @return the actual size of the queue
     * @throws MalformedObjectNameException
     */
    public long getCurrentQueueSize(CompomicsQueue queue) throws MalformedObjectNameException, PladipusTrafficException {
        long queueSize = 0;
        try {
            queueSize = cache.get(queue).getQueueSize();
        } catch (Exception e) {
            throw new PladipusTrafficException(e);
        }
        return queueSize;
    }

    /**
     *
     * @param queue the target queue
     * @return the amount of jobs passed to this queue since its creation
     * @throws MalformedObjectNameException
     */
    public long getCumulativeQueueSize(CompomicsQueue queue) throws MalformedObjectNameException, PladipusTrafficException {
        long queueSize = 0;
        try {
            queueSize = cache.get(queue).getEnqueueCount();
        } catch (Exception e) {
            throw new PladipusTrafficException(e);
        }
        return queueSize;
    }

    /**
     *
     * @param queue the target queue
     * @return the amount of jobs that have been pulled from this queue since
     * its creation
     * @throws MalformedObjectNameException
     */
    public long getPulledQueueSize(CompomicsQueue queue) throws MalformedObjectNameException, PladipusTrafficException {
        long queueSize = 0;
        try {
            queueSize = cache.get(queue).getDequeueCount();
        } catch (Exception e) {
            throw new PladipusTrafficException(e);
        }
        return queueSize;
    }

    /**
     *
     * @param queue the target queue
     * @return the amount of jobs that are being processed at this time
     * @throws MalformedObjectNameException
     */
    public long getInFlightSize(CompomicsQueue queue) throws MalformedObjectNameException, PladipusTrafficException {
        long queueSize = 0;
        try {
            queueSize = cache.get(queue).getInFlightCount();
        } catch (Exception e) {
            throw new PladipusTrafficException(e);
        }
        return queueSize;
    }

    /**
     *
     * @param queue the target queue
     * @param processID the id to query
     * @return whether this process is on the queue
     * @throws MalformedObjectNameException
     */
    public boolean isOnQueue(CompomicsQueue queue, int processID) throws MalformedObjectNameException, PladipusTrafficException {
        int onQueue = 0;
        try {
            CompositeData[] browseData = cache.get(queue).browse("JMSCorrelationID='" + processID + "'");
            onQueue = browseData.length;
        } catch (MalformedObjectNameException | OpenDataException | InvalidSelectorException e) {
            throw new PladipusTrafficException(e);
        }
        return onQueue > 0;
    }

    /**
     *
     * @param queue the target queue
     * @return the amount of consumers that are connected to this queue
     * @throws MalformedObjectNameException
     */
    public long getConsumerCount(CompomicsQueue queue) throws MalformedObjectNameException, PladipusTrafficException {
        long consumerCount = 0;
        try {
            consumerCount = cache.get(queue).getConsumerCount();
        } catch (Exception e) {
            throw new PladipusTrafficException(e);
        }
        return consumerCount;
    }

}
