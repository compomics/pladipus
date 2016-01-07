package com.compomics.pladipus.util;

import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ProcessDAO;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.DeleteOperation;
import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.jms.JMSException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * This class is used to stop runs
 *
 * @author compomics
 */
public class ProcessAction {

    private static final Logger LOGGER = Logger.getLogger(ProcessAction.class);

    public static void stopProcess(int... process_ids) {
        LOGGER.info("Stopping processes...");
        try {
            ProcessDAO pDAO = ProcessDAO.getInstance();
            List<Integer> processesToCancel = new ArrayList<Integer>();
            for (int processID : process_ids) {
                DeleteOperation dO = new DeleteOperation();
                dO.deleteJobFromQueue(CompomicsQueue.JOB, processID);
                pDAO.setQueued(processID, false);
            }
            pDAO.resetProcesses(processesToCancel);
        } catch (Exception ex) {
            LOGGER.error("Could not cancel the selection", ex);
        }
    }

    public static void startProcesses(int... process_ids) {
        try {
            ProcessDAO pDAO = ProcessDAO.getInstance();
            PladipusProcessingTemplate runTemplate = null;
            for (int processID : process_ids) {
                if (runTemplate == null) {
                    runTemplate = pDAO.getTemplate(processID);
                }
                //is the process complete?
                if (!pDAO.isCompletedProcess(processID) && !pDAO.isQueued(processID)) {
                    try (CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.JOB, pDAO.getXMLForProcess(processID), processID)) {
                        producer.run();
                    }
                    pDAO.setQueued(processID, true);
                }
            }
        } catch (JMSException | SQLException | IOException | StepLoadingException | ParserConfigurationException | SAXException ex) {
            LOGGER.error("Could not start the selection", ex);
        }
    }

}
