package com.compomics.pladipus.core.control.distribution.service;

import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ChainDAO;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ProcessDAO;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.RunDAO;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.CreateOperation;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.DeleteOperation;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.jms.JMSException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class RunService {

    /**
     * The runservice instance
     */
    private static RunService runService;

    /**
     *
     * @return an instance of the run service
     */
    public static RunService getInstance() {
        if (runService == null) {
            runService = new RunService();
        }
        return runService;
    }

    private RunService() {

    }

    /**
     *
     * @param runID
     * @return the owner of this run's mailing address
     * @throws SQLException
     */
    public String getOwnerContact(int runID) throws SQLException {
        try (RunDAO dao = RunDAO.getInstance()) {
            return dao.getOwnerContact(runID);
        }
    }

    /**
     *
     * @param user the owner of the runs
     * @return a map containing the run_Ids and their titles
     * @throws SQLException
     */
    public TreeMap<Integer, String> getRuns(String user) throws SQLException {
        try (RunDAO dao = RunDAO.getInstance()) {
            return dao.getRuns(user);
        }
    }

    // processing related methods
    /**
     *
     * @param template the template that will be used to launch tasks
     * @return the newly created run ID
     * @throws SQLException
     */
    public int createRun(PladipusProcessingTemplate template) throws SQLException {
        int runID = -1;
        try (RunDAO dao = RunDAO.getInstance(); ChainDAO cdao = ChainDAO.getInstance(); ProcessDAO pdao = ProcessDAO.getInstance()) {
            runID = dao.createRun(template);
            if (template.isKeepOrder() && runID != -1) {
                int nextChainId = cdao.getNextChainId();
                template.setChainID(nextChainId);
                LinkedList<Integer> processesForRun = pdao.getProcessesForRun(runID);
                cdao.addChain(runID, nextChainId, processesForRun);
            }
        }
        return runID;
    }

    /**
     *
     * @param runName the title of the run
     * @param userName the owner of the run
     * @return the ID corresponding to the provided information
     * @throws SQLException
     */
    public Integer getRunID(String runName, String userName) throws SQLException {
        try (RunDAO dao = RunDAO.getInstance()) {
            return dao.getRunID(runName, userName);
        }
    }

    public Integer getRunSize(int runID) throws SQLException {
        try (RunDAO dao = RunDAO.getInstance()) {
            return dao.getRunSize(runID);
        }
    }

    public boolean runExists(int runID) throws SQLException {
        try (RunDAO dao = RunDAO.getInstance()) {
            return dao.runExists(runID);
        }
    }

    /**
     *
     * @param runID the ID where current jobs should be added to
     * @param parameterList the parameters that should be used to create the new
     * jobs
     * @throws SQLException
     */
    public void addToRun(int runID, List<HashMap<String, String>> parameterList) throws SQLException, IOException, JMSException, SAXException, ParserConfigurationException, ProcessStepInitialisationException, ConnectionException {
        LinkedList<Integer> addToRun;
        try (RunDAO dao = RunDAO.getInstance()) {
            addToRun = dao.addToRun(runID, parameterList);
        }
        //add them to the queue
        if (addToRun != null) {
            CreateOperation cOperation = new CreateOperation();
            try (ProcessDAO dao = ProcessDAO.getInstance()) {
                for (int aProcessID : addToRun) {
                    String xmlForProcess = dao.getXMLForProcess(aProcessID);
                    cOperation.addJobToQueue(CompomicsQueue.JOB, xmlForProcess, aProcessID);
                }

            }

        }
    }

    /**
     *
     * @param runID the run to retrieve the template from
     * @return a jobXML using the template of the parent run and the parameters
     * stored in the database
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws ProcessStepInitialisationException
     */
    public PladipusProcessingTemplate getTemplateForRun(int runID) throws SQLException, IOException,
            ProcessStepInitialisationException, ParserConfigurationException, SAXException {
        try (RunDAO dao = RunDAO.getInstance()) {
            return dao.getTemplateForRun(runID);
        }
    }

    /**
     *
     * @param runID
     * @return a boolean indicating the completion state of this run
     * @throws SQLException
     */
    public boolean isCompletedRun(int runID) throws SQLException {
        try (RunDAO dao = RunDAO.getInstance()) {
            return dao.isCompletedRun(runID);
        }
    }

    /**
     *
     * @param runID
     * @return the owner of said run
     * @throws SQLException
     */
    public String getRunOwner(int runID) throws SQLException {
        try (RunDAO dao = RunDAO.getInstance()) {
            return dao.getRunOwner(runID);
        }
    }

    /**
     *
     * @param runID
     * @return the title of said run
     * @throws SQLException
     */
    public String getRunTitle(int runID) throws SQLException {
        try (RunDAO dao = RunDAO.getInstance()) {
            return dao.getRunTitle(runID);
        }
    }

    public int removeRun(List<Integer> runIDs) throws SQLException, IOException, Exception {
        HashMap<Integer, Collection<Integer>> processIDperRunID = new HashMap<>();

        try (ProcessDAO pDAO = ProcessDAO.getInstance()) {
            for (int runID : runIDs) {
                processIDperRunID.put(runID, pDAO.getProcessesForRun(runID));
            }
        }
        try (RunDAO dao = RunDAO.getInstance()) {
            dao.deleteRun(runIDs);
        }
        //also delete from queue
        DeleteOperation delete = new DeleteOperation();
        int totalDeleteCount = 0;
        for (Map.Entry<Integer, Collection<Integer>> aRunEntry : processIDperRunID.entrySet()) {
            delete.deleteJobsFromQueue(CompomicsQueue.JOB, aRunEntry.getValue());
            totalDeleteCount += aRunEntry.getValue().size();
        }
        return totalDeleteCount;
    }

    /**
     * Removes all run, job and parameter information from the database
     *
     * @throws SQLException
     */
    public void truncate() throws SQLException {
        try (RunDAO dao = RunDAO.getInstance()) {
            dao.truncatePladipus();
        }
        //TODO purge the queue's as well !
    }

}
