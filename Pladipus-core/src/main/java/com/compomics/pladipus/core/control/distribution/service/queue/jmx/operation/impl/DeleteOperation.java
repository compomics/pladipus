/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl;

import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ProcessDAO;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.QueueOperation;
import com.compomics.pladipus.core.model.exception.PladipusTrafficException;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     *
     */
    public void purgeQueue(CompomicsQueue queue) throws PladipusTrafficException, MalformedObjectNameException {
        try {
            cache.get(queue).purge();
        } catch (Exception ex) {
            throw new PladipusTrafficException(ex);
        }
    }

    /**
     * Removes a single job from the queue
     *
     * @param queue the target queue
     * @param processID the specific job ID
     * @return the amount of messages removed
     * @throws MalformedObjectNameException
     */
    public int deleteJobFromQueue(CompomicsQueue queue, int processID) throws MalformedObjectNameException, PladipusTrafficException {
        int removeMatchingMessages = 0;
        try {
            removeMatchingMessages = cache.get(queue).removeMatchingMessages("JMSCorrelationID = '" + processID + "'");
        } catch (Exception ex) {
            throw new PladipusTrafficException(ex);
        }
        return removeMatchingMessages;
    }

    /**
     * Removes a collection of jobs from a specific queue
     *
     * @param queue the target queue
     * @param processIDs
     * @return amount of removed processes
     * @throws MalformedObjectNameException
     */
    public int deleteJobsFromQueue(CompomicsQueue queue, Collection<Integer> processIDs) throws MalformedObjectNameException, PladipusTrafficException {
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
     * @throws java.sql.SQLException
     */
    public int deleteRunFromQueue(CompomicsQueue queue, int runID) throws MalformedObjectNameException, PladipusTrafficException, SQLException {
        return deleteJobsFromQueue(queue, ProcessDAO.getInstance().getProcessesForRun(runID));
    }

}
