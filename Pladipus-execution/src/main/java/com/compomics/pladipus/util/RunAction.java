package com.compomics.pladipus.util;

import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ProcessDAO;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.RunDAO;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.DeleteOperation;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import javax.jms.JMSException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * This class is used to stop runs
 *
 * @author compomics
 */
public class RunAction {

    private static final Logger LOGGER = Logger.getLogger(RunAction.class);

    public static void stopRuns(int... run_ids) {
        LOGGER.info("Stopping runs...");
        ProcessDAO dao = ProcessDAO.getInstance();
        for (int run_id : run_ids) {
            try {
                LinkedList<Integer> queuedProcess = dao.getQueuedProcesses(run_id);
                LOGGER.info("Halting " + queuedProcess.size() + " processes that are currently queued for " + run_id);
                for (int aProcessID : queuedProcess) {
                    DeleteOperation dO = new DeleteOperation();
                    dO.deleteJobFromQueue(CompomicsQueue.JOB, aProcessID);
                }
                dao.setQueued(queuedProcess, false);
            } catch (Exception ex) {
            }
        }
    }

    public static void startRuns(int... run_ids) {
        LOGGER.info("Starting runs...");
        ProcessDAO dao = ProcessDAO.getInstance();
        RunDAO rDao = RunDAO.getInstance();
        for (int run_ID : run_ids) {
            try {
                PladipusProcessingTemplate templateForRun = rDao.getTemplateForRun(run_ID);
                Collection<ProcessingJob> unqueuedProcesses = dao.getJobsForRun(templateForRun, run_ID, false, false);
                Collection<Integer> processesToQueue = new ArrayList<>();
                LOGGER.info("Dispatching " + unqueuedProcesses.size() + " processes for " + run_ID);
                for (ProcessingJob aJob : unqueuedProcesses) {
                    long processID = aJob.getId();
                    try (
                            CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.JOB, templateForRun.getPriority())) {
                        producer.addMessage(aJob.toXML(), (int) processID);
                        producer.run();
                    }
                    processesToQueue.add((int) processID);
                }
                dao.setQueued(processesToQueue, true);
            } catch (JMSException | NumberFormatException | SQLException | IOException | ProcessStepInitialisationException | ParserConfigurationException | SAXException ex) {
                LOGGER.error("Could not launch the selection.", ex);
            }
        }
    }

}
