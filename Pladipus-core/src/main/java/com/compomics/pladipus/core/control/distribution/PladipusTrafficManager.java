package com.compomics.pladipus.core.control.distribution;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.distribution.service.ProcessService;
import com.compomics.pladipus.core.control.distribution.service.RunService;
import com.compomics.pladipus.core.control.distribution.service.UserService;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.UserDAO;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.impl.CompomicsDurableConsumer;
import com.compomics.pladipus.core.control.distribution.service.queue.impl.CompomicsSessionConsumer;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.QueryOperation;
import com.compomics.pladipus.core.model.exception.PladipusTrafficException;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import javax.jms.JMSException;
import javax.naming.AuthenticationException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class PladipusTrafficManager {

    /**
     * The traffic manager instance
     */
    private static PladipusTrafficManager instance;
    /**
     * The a Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(PladipusTrafficManager.class);
    /**
     * The default priority
     */
    private static int priority = 4;

    public static PladipusTrafficManager getInstance() {
        if (instance == null) {
            instance = new PladipusTrafficManager();
        }
        return instance;
    }

    private PladipusTrafficManager() {

    }

    private boolean isValidUser(String user) throws AuthenticationException {
        //check if the user even exists, if not ---> reject !
        String userContactAddress;
        try {
            UserService service = UserService.getInstance();
            userContactAddress = service.getUserContactAddress(user);
            if (userContactAddress == null || userContactAddress.isEmpty()) {
                throw new AuthenticationException(user + " does not exist in the Pladipus database. Please contact the admin");
            } else {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            LOGGER.error("SQL database could not be reached ! " + ex);
        }
        return false;
    }

    /**
     * Pushes tasks to the pladipus queue. In case the run already exists or was
     * posted before, the jobs will be appended after the existing jobs
     *
     * @param convertXMLtoTemplate
     * @param jobConfigurationFile the job configuration file
     * @throws IOException
     * @throws AuthenticationException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws SQLException
     * @throws JMSException
     * @throws ProcessStepInitialisationException
     */
    public void pushToPladipus(PladipusProcessingTemplate convertXMLtoTemplate, File jobConfigurationFile) throws IOException, AuthenticationException, ParserConfigurationException, SAXException, SQLException, JMSException, ProcessStepInitialisationException, ConnectionException {
        if (!jobConfigurationFile.exists()) {
            throw new IllegalArgumentException("Job configuration file does not exist!");
        }
        LOGGER.info("Posting jobs to the pladipus network...");
        XMLTemplateInterpreter interpreter = XMLTemplateInterpreter.getInstance();
        LOGGER.debug("Reading provided job configuration file...");
        String runTitle = convertXMLtoTemplate.getName();
        String xmlUser = convertXMLtoTemplate.getUser();
        RunService rService = RunService.getInstance();
        if (isValidUser(xmlUser)) {
            priority = convertXMLtoTemplate.getPriority();
            LOGGER.debug("Storing local jobs for " + xmlUser + " run name = " + runTitle);
            LinkedList<HashMap<String, String>> readLocalProcessingParameters = interpreter.readLocalProcessingParameters(convertXMLtoTemplate, jobConfigurationFile);
            LOGGER.debug("Converting template to job using the provided configuration...");
            LOGGER.debug("Finding run...");
            int runId = rService.getRunID(runTitle, xmlUser);
            LOGGER.debug("Matching run found : runID = " + runId);
            LOGGER.debug("Adding " + readLocalProcessingParameters.size() + " jobs to run...");
            rService.addToRun(runId, readLocalProcessingParameters);
        }
    }

    /**
     * Pushes tasks to the pladipus queue. In case the run already exists or was
     * posted before, the jobs will be appended after the existing jobs
     *
     * @param templateFile the template file for the run
     * @param jobConfigurationFile the job configuration file
     * @return the generated run id
     * @throws IOException
     * @throws AuthenticationException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws SQLException
     * @throws JMSException
     * @throws ProcessStepInitialisationException
     */
    public int pushToPladipus(File templateFile, File jobConfigurationFile) throws IOException, AuthenticationException, ParserConfigurationException, SAXException, SQLException, JMSException, ProcessStepInitialisationException, ConnectionException {

        if (!templateFile.exists()) {
            throw new IllegalArgumentException("Template file does not exist !");
        }
        if (!jobConfigurationFile.exists()) {
            throw new IllegalArgumentException("Job configuration file does not exist!");
        }
        LOGGER.info("Posting jobs to pladipus...");

        XMLTemplateInterpreter interpreter = XMLTemplateInterpreter.getInstance();
        LOGGER.debug("Reading provided job configuration file...");
        PladipusProcessingTemplate convertXMLtoTemplate = interpreter.convertXMLtoTemplate(templateFile);
        String runTitle = convertXMLtoTemplate.getName();
        String xmlUser = convertXMLtoTemplate.getUser();
        RunService rService = RunService.getInstance();
        int runId = -1;
        if (isValidUser(xmlUser)) {
            priority = convertXMLtoTemplate.getPriority();
            LOGGER.debug("Storing local jobs for " + xmlUser + " run name = " + runTitle);
            LinkedList<HashMap<String, String>> readLocalProcessingParameters = interpreter.readLocalProcessingParameters(convertXMLtoTemplate, jobConfigurationFile);
            LOGGER.debug("Converting template to job using the provided configuration...");
            LOGGER.debug("Finding run...");
            runId = rService.getRunID(runTitle, xmlUser);
            if (runId == -1) {
                runId = rService.createRun(convertXMLtoTemplate);
                LOGGER.debug("Run created : runID = " + runId);
            } else {
                LOGGER.debug("Matching run found : runID = " + runId);
            }
            LOGGER.debug("Adding " + readLocalProcessingParameters.size() + " jobs to run...");
            ProcessService pService = ProcessService.getInstance();
            rService.addToRun(runId, readLocalProcessingParameters);
            //only the not-queued ones should be put on the queue here 
            LinkedList<Integer> processesForRun = pService.getProcessesForRun(runId);
            pushFromDatabase(processesForRun);
        }
        return runId;
    }

    /**
     * Pushes all database entries that have not been set on the activeMQ queue
     * yet. This method is soon to be in case the mysql and khahadb can be
     * merged
     *
     * @param processesForRun sets the jobs to be added
     * @throws IOException
     * @throws SAXException
     * @throws JMSException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws ProcessStepInitialisationException
     */
    public void pushFromDatabase(LinkedList<Integer> processesForRun) throws IOException, SAXException, JMSException, SQLException, ParserConfigurationException, ProcessStepInitialisationException {
        LOGGER.info("Pushing " + processesForRun.size() + " jobs to Pladipus...");
        ProcessService pService = ProcessService.getInstance();
        try (CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.JOB, priority)) {
            for (int aProcessID : processesForRun) {
                String xmlForProcess = pService.getXMLForProcess(aProcessID);
                producer.addMessage(xmlForProcess, aProcessID);
            }
            Thread producerThread = new Thread(producer, "ProducerThread");
            producerThread.start();
        }
    }

    /**
     * Draws a job from the queue and executes it
     *
     * @throws PladipusTrafficException
     */
    public void pullFromPladipus() throws PladipusTrafficException {
        try {
            pullUpdates();
            CompomicsSessionConsumer compomicsSessionConsumer = new CompomicsSessionConsumer();
            compomicsSessionConsumer.run();
        } catch (IOException | JMSException ex) {
            throw new PladipusTrafficException("Could not see the ActiveMQ service");
        }
    }

    /**
     * Checks for system broadcasts and executes them
     * @throws JMSException
     * @throws IOException
     */
    public void pullUpdates() throws JMSException, IOException {
        CompomicsDurableConsumer compomicsSessionConsumer = new CompomicsDurableConsumer(CompomicsQueue.UPDATE);
        compomicsSessionConsumer.run();
    }

    //todo make this robust

    /**
     * checks if the database and the activemq servers are reachable
     * @return true if successful
     * @throws SQLException
     * @throws ConnectionException
     * @throws IOException
     */
    public boolean isSystemOnline() throws SQLException,ConnectionException,IOException {
            //do a quick check on queue
            new QueryOperation();
            //do a quick check on the sql database
            UserDAO dao = UserDAO.getInstance();
        //todo this is just no good
        dao.userExists("check");
            return true;
    }
}
