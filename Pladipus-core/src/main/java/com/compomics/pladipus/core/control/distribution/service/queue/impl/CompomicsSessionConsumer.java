package com.compomics.pladipus.core.control.distribution.service.queue.impl;

import com.compomics.pladipus.core.control.distribution.communication.mail.Mailer;
import com.compomics.pladipus.core.control.distribution.communication.mail.ReportGenerator;
import com.compomics.pladipus.core.control.distribution.communication.mail.impl.StandardReportGenerator;
import com.compomics.pladipus.core.control.distribution.service.ChainService;
import com.compomics.pladipus.core.control.distribution.service.ProcessService;
import com.compomics.pladipus.core.control.distribution.service.RunService;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsConsumer;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsQueueConnectionFactory;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.DeleteOperation;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.engine.impl.SessionProcessingEngine;
import com.compomics.pladipus.core.model.exception.BrokenProcessChainException;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.PladipusTrafficException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.exception.XMLInterpreterException;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.LinkedHashMap;
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
import javax.mail.MessagingException;
import javax.management.MalformedObjectNameException;
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

    public CompomicsSessionConsumer(CompomicsQueue queue) throws IOException, JMSException {
        super(queue);
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
                            pService.getProcessingJob(processID).getPriority())) {
                producer.addMessage(textMessage.getText(), (int) processID);
                producer.run();
            } catch (Exception ex) {
                LOGGER.warn("Could not relaunch job : " + processID + ". Please try again manually or by force-starting the entire run");
                LOGGER.error(ex);
            }
        } else {
            //set the entire chain to that failcount...
            ChainService cService = ChainService.getInstance();
            int chainIDForProcess = cService.getChainIDForProcess(processID);
            //fail all the processes in this chain...
            if (chainIDForProcess != -1) {
                LinkedHashMap<Integer, String> workersOnSameChain = cService.getWorkersOnSameChain(chainIDForProcess);
                for (int aProcessInTheChain : workersOnSameChain.keySet()) {
                    pService.increaseFailCount(aProcessInTheChain, maxFailCount);
                }
                //remove these from the queue?
                try {
                    DeleteOperation deleteOperation = new DeleteOperation();
                    deleteOperation.deleteJobsFromQueue(queue, workersOnSameChain.keySet());
                    LOGGER.info("Maximal failcount was reached for part of a chain, causing the chain to fail and its processes to be removed from the queue!");
                } catch (PladipusTrafficException | ConnectionException | MalformedObjectNameException e) {
                    LOGGER.error("Maximal failcount was reached for a chain of processes, but pladipus could not remove them from the queue...", e);
                }
            } else {
                LOGGER.info("Maximal failcount was reached, not resetting the job !");
            }

        }
    }

    /**
     * Rolls back a message without storing an error (example in case of
     * prerequisite mismatching)
     *
     * @throws JMSException
     */
    public void rollback() throws JMSException {
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
        Throwable unwrappedException = null;
        boolean harmlessException = false;
        if (message instanceof TextMessage) {
            Future<Boolean> future;
            try {
                future = executor.submit(new SessionProcessingEngine(message));
                //executor.shutdown();
                //executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
                if (future.get(1, TimeUnit.DAYS)) {
                    commit(message);
                    LOGGER.info("Completed task");
                }
            } catch (ExecutionException ex) {
                unwrappedException = (ex.getCause().getCause());
                if (unwrappedException instanceof PladipusTrafficException
                        | unwrappedException instanceof XMLInterpreterException
                        | unwrappedException instanceof SQLException
                        | unwrappedException instanceof RejectedExecutionException
                        | unwrappedException instanceof BrokenProcessChainException) {
                    harmlessException = true;
                } else if (unwrappedException instanceof PladipusProcessingException
                        | unwrappedException instanceof UnspecifiedPladipusException) {
                    harmlessException = false;
                }
            } catch (IOException | InterruptedException | TimeoutException | SQLException ex) {
                //these exceptions have nothing to do with the job, as they don't come from the future...
                LOGGER.error(ex);
                harmlessException = true;
            } finally {
                if (unwrappedException != null) {
                    if (harmlessException) {
                        String exceptionMessage = unwrappedException.getMessage();
                        LOGGER.info(exceptionMessage);
                        rollback();
                    } else {
                        try {
                            CallbackNotifier callbackNotifier = new CallbackNotifier(message);
                            callbackNotifier.onNotification(unwrappedException.getMessage(), false);
                            rollbackWithError(message);
                            //log the error to a file-->should be refactored to get a spot in the database or something, or mail? :
                            File errorLog = new File(System.getProperty("user.home") + "/pladipus/log/errors.log");
                            errorLog.getParentFile().mkdirs();
                            errorLog.createNewFile();
                            try ( //APPEND EXCEPTION TO THE FILE
                                    PrintWriter writer = new PrintWriter(errorLog)) {
                                writer.append("ERROR IN PROCESS : " + message.getJMSCorrelationID() + System.lineSeparator()).flush();
                                unwrappedException.printStackTrace(writer);
                                unwrappedException.printStackTrace();
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
}
