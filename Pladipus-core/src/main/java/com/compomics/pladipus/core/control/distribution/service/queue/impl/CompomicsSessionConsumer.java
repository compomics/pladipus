/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue.impl;

import com.compomics.pladipus.core.control.distribution.communication.mail.Mailer;
import com.compomics.pladipus.core.control.distribution.communication.mail.ReportGenerator;
import com.compomics.pladipus.core.control.distribution.communication.mail.impl.StandardReportGenerator;
import com.compomics.pladipus.core.control.distribution.service.ProcessService;
import com.compomics.pladipus.core.control.distribution.service.RunService;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsConsumer;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsQueueConnectionFactory;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.engine.impl.SessionProcessingEngine;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class CompomicsSessionConsumer extends CompomicsConsumer {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CompomicsSessionConsumer.class);
    /**
     * The Queue connection factory (for JMX / ACTIVEMQ)
     */
    CompomicsQueueConnectionFactory connectionFactory = CompomicsQueueConnectionFactory.getInstance();

    public CompomicsSessionConsumer() throws IOException, JMSException {
        super(CompomicsQueue.JOB);
    }

    /**
     * commits the message if it was successfull. Resets it on the queue in case
     * of failure/prerequisite mismatch
     *
     * @param message the recieved message
     * @throws JMSException
     * @throws IOException
     * @throws SQLException
     */
    public void commit(Message message) throws JMSException, IOException, SQLException {
        connectionFactory.getSession().commit();
        terminateProcess(message);
    }

    /**
     * Rolls back the message while storing the error in the database
     *
     * @param message
     * @throws JMSException
     * @throws IOException
     * @throws SQLException
     */
    public void rollbackWithError(Message message) throws JMSException, IOException, SQLException {
        //add a fail to the counter and check the failcount
        ProcessService pService = ProcessService.getInstance();
        int processID = Integer.parseInt(message.getJMSCorrelationID());
        int failcount = pService.getFailCount(processID);
        int maxFailCount = NetworkProperties.getInstance().getMaxFailCount();
        if (failcount < maxFailCount) {
            pService.increaseFailCount(processID);
            rollback();
        }
    }

    /**
     * Rolls back a message without storing an error (example in case of
     * prerequisite mismatching)
     *
     * @throws JMSException
     */
    public void rollback() throws JMSException {
        LOGGER.info("Rolling back job...");
        connectionFactory.getSession().rollback();
    }

    private void terminateProcess(Message message) throws IOException, SQLException, JMSException {
        ProcessService pService = ProcessService.getInstance();
        RunService rService = RunService.getInstance();
        int processID = Integer.parseInt(message.getJMSCorrelationID());
        pService.setDone(processID);
        int parentRunID = pService.getParentRunID(processID);
        if (rService.isCompletedRun(parentRunID)) {
            ReportGenerator generator = new StandardReportGenerator();
            String recipient = rService.getOwnerContact(parentRunID);
            LOGGER.info("Run complete, notifying " + recipient);
            Mailer mailer = new Mailer(recipient);
            mailer.sendMail(generator.generateSubject(parentRunID), generator.generateReport(parentRunID));
        }
    }

    @Override
    public void processMessage(Message message) throws JMSException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        if (message instanceof TextMessage) {

            try {
                Future<Boolean> future = executor.submit(new SessionProcessingEngine(message));
                //executor.shutdown();
                //executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
                if (future.get(1, TimeUnit.DAYS)) {
                    commit(message);
                    LOGGER.info("Succesfully handled task");
                }
            } catch (JMSException | IOException | InterruptedException | ExecutionException | TimeoutException | SQLException ex) {
                //otherwise other executionexceptions are ignored...
                if (ex instanceof RejectedExecutionException) {
                    LOGGER.info("Machine is not qualified to run this job");
                    rollback();
                } else {
                    try {
                        CallbackNotifier callbackNotifier = CallbackNotifier.getInstance();
                        callbackNotifier.onNotification(ex.getMessage(), message);
                        rollbackWithError(message);
                    } catch (IOException | SQLException ex1) {
                        ex1.printStackTrace();
                        LOGGER.error(ex1);
                    }
                }
            }
        }
    }

}
