/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ProcessDAO;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.DeleteOperation;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.QueryOperation;
import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessService {

    /**
     * The processService instance
     */
    private static ProcessService processService;

    /**
     *
     * @return the processing service instance
     */
    public static ProcessService getInstance() {
        if (processService == null) {
            processService = new ProcessService();
        }
        return processService;
    }

    private ProcessService() {

    }

    /**
     *
     * @param processID
     * @return the amount of occurences where this process has faield
     * @throws SQLException
     */
    public int getFailCount(int processID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getFailCount(processID);
        }
    }

    /**
     *
     * @param processID
     * @return the amount of steps this process has
     * @throws SQLException
     */
    public int getStepCount(int processID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getStepCount(processID);
        }
    }

    /**
     *
     * @param processID
     * @return the run ID of the parent of this process
     * @throws SQLException
     */
    public int getParentRunID(int processID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getParentRunID(processID);
        }
    }

    /**
     *
     * @param processID the ID of the process
     * @return the state the process was last in
     * @throws SQLException
     */
    public String getProcessState(int processID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getProcessState(processID);
        }
    }

    /**
     *
     * @param runID the parent run to be queried
     * @return a map of processID's with their respective states
     * @throws SQLException
     */
    public TreeMap<Integer, String> getProcessStatesForRun(int runID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getProcessStatesForRun(runID);
        }
    }

    /**
     *
     * @param runID
     * @return a list or processes for this parent run
     * @throws SQLException
     */
    public LinkedList<Integer> getProcessesForRun(int runID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getProcessesForRun(runID);
        }
    }

    /**
     *
     * @return a list or processes for this parent run
     * @throws SQLException
     */
    public LinkedList<Integer> getUnqueuedProcesses() throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getUnqueuedProcesses();
        }
    }

    /**
     *
     * @param user the owner of the processes
     * @return a list of processIDs that have been submitted, but have yet to be
     * put on queue
     * @throws SQLException
     */
    public LinkedList<Integer> getUnqueuedProcesses(String user) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getUnqueuedProcesses(user);
        }
    }

    /**
     *
     * @param processID the process to be launched
     * @return a jobXML using the template of the parent run and the parameters
     * stored in the database
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws StepLoadingException
     */
    public String getXMLForProcess(int processID) throws SQLException, SAXException,
            IOException, ParserConfigurationException, StepLoadingException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getXMLForProcess(processID);
        }
    }

    /**
     * Resets the process, but keeps track of the failcounter
     *
     * @param processID
     * @throws SQLException
     */
    public void increaseFailCount(int processID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            dao.increaseFailCount(processID);
            dao.resetStepCount(processID);
        }
    }

    /**
     *
     * @param processID
     * @return a boolean indicating if this process has been terminated (this
     * can be either due to completion or repeated failure)
     * @throws SQLException
     */
    public boolean isCompletedProcess(int processID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.isCompletedProcess(processID);
        }
    }

    /**
     *
     * @param runID
     * @return the amount of completed processes for a run
     * @throws SQLException
     */
    public double getCompleteProcessCount(int runID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getCompletedProcessesForRun(runID);
        }
    }

    /**
     *
     * @param processID
     * @return a boolean indicating if this process has been put on the queue
     * @throws SQLException
     */
    public boolean isQueued(int processID) throws IOException, Exception {
        QueryOperation queryOperation = new QueryOperation();
        return queryOperation.isOnQueue(CompomicsQueue.JOB, processID);
    }

    /**
     *
     * @param selectedProcessIDs a list of ids to be removed
     * @throws SQLException
     */
    public void removeProcesses(List<Integer> selectedProcessIDs) throws SQLException, Exception {
        //remove them from the queue first
        DeleteOperation deleteOperation = new DeleteOperation();
        deleteOperation.deleteJobsFromQueue(CompomicsQueue.JOB, selectedProcessIDs);
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            dao.removeProcesses(selectedProcessIDs);
        }
    }

    /**
     *
     * @param selectedProcessIDs a list of ids to be removed
     * @throws SQLException
     */
    public void removeProcess(int selectedProcessID, boolean removeFromDb) throws SQLException, Exception {
        //remove them from the queue first
        DeleteOperation deleteOperation = new DeleteOperation();
        deleteOperation.deleteJobFromQueue(CompomicsQueue.JOB, selectedProcessID);
        if (removeFromDb) {
            try (ProcessDAO dao = ProcessDAO.getInstance()) {
                dao.removeProcess(selectedProcessID);
            }
        }
    }

    /**
     *
     * @param processID the processID
     * @return returns a processingJob object for this processID
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws Exception
     */
    public ProcessingJob retrieveProcessingJob(int processID) throws SQLException, SAXException, IOException, Exception {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.retrieveProcessingJob(processID);
        }
    }

    /**
     *
     * @param aProcessID the processID that should be set complete
     * @throws SQLException
     */
    public void setDone(Integer aProcessID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            dao.setDone(aProcessID);
        }
    }

    /**
     *
     * @param processID the process to be updated
     * @param newState the new state for the process
     * @throws SQLException
     */
    public void updateProcessState(int processID, String newState) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            dao.updateProcessState(processID, newState);
        }
    }

    /**
     *
     * @param processID the process to be updated
     * @throws SQLException
     */
    public void increaseProcessStepCount(int processID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            dao.updateProcessCounter(processID);
        }
    }

    /**
     *
     * @param processID
     * @return the job object for this process ID
     * @throws Exception
     */
    public ProcessingJob getProcessingJob(int processID) throws Exception {
        return XMLJobInterpreter.getInstance().convertXMLtoJob(getXMLForProcess(processID));
    }

    /**
     * Updates the parameter collection for a process (CAUTION, only parameters
     * that are specified in the run template are allowed)
     *
     * @param processID the process to be updated
     * @param processingParameters the new process parameters
     * @throws SQLException
     */
    public void updateProcessParameters(int processID, HashMap<String, String> processingParameters) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            dao.updateProcessParameters(processID, processingParameters);
        }
    }

    /**
     * Resets the given process
     *
     * @param processID
     * @throws SQLException
     */
    public void resetProcess(int processID) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            dao.resetProcess(processID);
        }
    }

    /**
     * Resets the given processes
     *
     * @param processID
     * @throws SQLException
     */
    public void resetProcesses(Collection<Integer> processIDs) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            dao.resetProcesses(processIDs);
        }
    }

    /**
     *
     * @param runID the run ID
     * @param lowestLimit the lower limit of jobs to pull
     * @param upperLimit the upper limit of jobs to pull
     * @return a list of process execution metadata
     * @throws SQLException
     */
    public LinkedList<Object[]> getProcessInformation(int runID, int lowestLimit, int upperLimit) throws SQLException {
        try (ProcessDAO dao = ProcessDAO.getInstance()) {
            return dao.getProcessInformation(runID, lowestLimit, upperLimit);
        }
    }

}
