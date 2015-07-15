/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl;

import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.QueueOperation;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.IOException;
import javax.management.MalformedObjectNameException;
import javax.management.openmbean.CompositeData;

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
     * @throws Exception
     */
    public long getCurrentQueueSize(CompomicsQueue queue) throws MalformedObjectNameException, Exception {
        return cache.get(queue).getQueueSize();
    }

    /**
     *
     * @param queue the target queue
     * @return the amount of jobs passed to this queue since its creation
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public long getCumulativeQueueSize(CompomicsQueue queue) throws MalformedObjectNameException, Exception {
        return cache.get(queue).getEnqueueCount();
    }

    /**
     *
     * @param queue the target queue
     * @return the amount of jobs that have been pulled from this queue since
     * its creation
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public long getPulledQueueSize(CompomicsQueue queue) throws MalformedObjectNameException, Exception {
        return cache.get(queue).getDequeueCount();
    }

    /**
     *
     * @param queue the target queue
     * @return the amount of jobs that are being processed at this time
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public long getInFlightSize(CompomicsQueue queue) throws MalformedObjectNameException, Exception {
        return cache.get(queue).getInFlightCount();
    }

    /**
     *
     * @param queue the target queue
     * @param processID the id to query
     * @return whether this process is on the queue
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public boolean isOnQueue(CompomicsQueue queue, int processID) throws MalformedObjectNameException, Exception {
        CompositeData[] browseData = cache.get(queue).browse("JMSCorrelationID='" + processID + "'");
        int onQueue = browseData.length;
        return onQueue > 0;
    }

    /**
     *
     * @param queue the target queue
     * @return the amount of consumers that are connected to this queue
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public long getConsumerCount(CompomicsQueue queue) throws MalformedObjectNameException {
        return cache.get(queue).getConsumerCount();
    }

}
