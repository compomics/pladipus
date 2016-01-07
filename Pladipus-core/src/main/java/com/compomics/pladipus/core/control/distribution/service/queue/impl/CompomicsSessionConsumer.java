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
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsQueueConnectionFactory;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.engine.impl.SessionProcessingEngine;
import com.compomics.pladipus.core.model.processing.standard.maintenance.RebootStep;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.mail.MessagingException;
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
            //repush the message
            TextMessage textMessage = (TextMessage) message;
            try (
                    CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.JOB,
                            textMessage.getText(),
                            (int) processID,
                            pService.getProcessingJob(processID).getPriority())) {
                producer.run();
            } catch (Exception ex) {
                LOGGER.warn("Could not relaunch job : " + processID + ". Please try again manually or by force-starting the entire run");
                LOGGER.error(ex);
            }
        }else{
            LOGGER.info("Maximal failcount was reached, not resetting the job !");
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
            try {
                mailer.generateAndSendEmail(generator.generateSubject(parentRunID), generator.generateReport(parentRunID), recipient);
            } catch (MessagingException ex) {
                LOGGER.warn("Could not notify run owner of finished run !");
            }
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
                    LOGGER.info("Completed task");
                }
                //exception can be due to nearly everything. it is up to the user to chose what to throw...
            } catch (Exception ex) {
                //otherwise other executionexceptions are ignored...
                if (ex instanceof RejectedExecutionException) {
                    LOGGER.info("Machine is not qualified to run this job");
                    rollback();
                } else {
                    try {
                        CallbackNotifier callbackNotifier = new CallbackNotifier(message);
                        callbackNotifier.onNotification(ex.getMessage(), false);
                        rollbackWithError(message);
                        //log the error to a file-->should be refactored to get a spot in the database or something, or mail? :
                        File errorLog = new File(System.getProperty("user.home") + "/pladipus/log/errors.log");
                        errorLog.getParentFile().mkdirs();
                        errorLog.createNewFile();
                        try ( //APPEND EXCEPTION TO THE FILE
                                PrintWriter writer = new PrintWriter(errorLog)) {
                            writer.append("ERROR IN PROCESS : " + message.getJMSCorrelationID() + System.lineSeparator()).flush();
                            ex.printStackTrace(writer);
                            ex.printStackTrace();
                        }
                    } catch (IOException | SQLException ex1) {
                        ex1.printStackTrace();
                        LOGGER.error(ex1);
                    }
                }
            }
        }
    }

}
