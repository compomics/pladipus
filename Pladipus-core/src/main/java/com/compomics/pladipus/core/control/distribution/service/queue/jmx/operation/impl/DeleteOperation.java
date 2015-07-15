/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl;

import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ProcessDAO;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.QueueOperation;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.IOException;
import java.util.Collection;
import javax.management.MalformedObjectNameException;

/**
 *
 * @author Kenneth Verheggen
 */
public class DeleteOperation extends QueueOperation {

    public DeleteOperation() throws IOException, ConnectionException {
        super();
    }

    /**
     * Removes all messages from a specified queue
     *
     * @param queue the target queue
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public void purgeQueue(CompomicsQueue queue) throws MalformedObjectNameException, Exception {
        cache.get(queue).purge();
    }

    /**
     * Removes a single job from the queue
     *
     * @param queue the target queue
     * @param processID the specific job ID
     * @return the amount of messages removed
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public int deleteJobFromQueue(CompomicsQueue queue, int processID) throws MalformedObjectNameException, Exception {
        return cache.get(queue).removeMatchingMessages("JMSCorrelationID = '" + processID + "'");
    }

    /**
     * Removes a collection of jobs from a specific queue
     *
     * @param queue the target queue
     * @param processIDs
     * @return amount of removed processes
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public int deleteJobsFromQueue(CompomicsQueue queue, Collection<Integer> processIDs) throws MalformedObjectNameException, Exception {
        int deleteCounter = 0;
        for (int processID : processIDs) {
            deleteCounter += deleteJobFromQueue(queue, processID);
        }
        return deleteCounter;
    }

    /**
     * Removes all jobs belonging to a specified run from a target queue
     *
     * @param queue the specified queue
     * @param runID the target run to delete
     * @return
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public int deleteRunFromQueue(CompomicsQueue queue, int runID) throws MalformedObjectNameException, Exception {
        return deleteJobsFromQueue(queue, ProcessDAO.getInstance().getProcessesForRun(runID));
    }

}
