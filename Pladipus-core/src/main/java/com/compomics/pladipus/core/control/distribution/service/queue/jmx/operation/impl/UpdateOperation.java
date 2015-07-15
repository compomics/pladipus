/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl;

import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.QueueOperation;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.IOException;
import javax.management.MalformedObjectNameException;

/**
 *
 * @author Kenneth Verheggen
 */
public class UpdateOperation extends QueueOperation {

    public UpdateOperation() throws IOException, ConnectionException {
        super();
    }

    /**
     * Updates an existing, already launched job 
     *
     * @param queue the target queue
     * @param newMessage the new message
     * @param processID the target job id
     * @throws MalformedObjectNameException
     * @throws Exception
     */
    public void updateJobOnQueue(CompomicsQueue queue, String newMessage, int processID) throws MalformedObjectNameException, Exception {
        //delete using the DeleteOperation
        DeleteOperation deleteOperation = new DeleteOperation();
        deleteOperation.deleteJobFromQueue(queue, processID);
        //push the new job using the processID
        CompomicsProducer producer = new CompomicsProducer(queue, newMessage, processID);
        Thread producerThread = new Thread(producer, "ProducerThread");
        producerThread.start();
    }

}
