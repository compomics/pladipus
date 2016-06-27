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
import java.sql.SQLException;
import javax.jms.JMSException;

/**
 *
 * @author Kenneth Verheggen
 */
public class CreateOperation extends QueueOperation {

    public CreateOperation() throws IOException, ConnectionException {
        super();
    }

    /**
     * Posts a job to the specified Queue
     *
     * @param queue the target queue
     * @param jobAsXML the job as an XML
     * @param processID the process id for the job
     * @throws IOException
     * @throws JMSException
     * @throws SQLException
     */
    public void addJobToQueue(CompomicsQueue queue, String jobAsXML, int processID) throws IOException, JMSException, SQLException {
        //TODO REFACTOR THIS TO USE A PRODUCERPOOL?
        try (CompomicsProducer producer = new CompomicsProducer(queue)) {
            producer.addMessage(jobAsXML, processID);
            Thread producerThread = new Thread(producer, "ProducerThread");
            producerThread.start();
        }
    }

}
