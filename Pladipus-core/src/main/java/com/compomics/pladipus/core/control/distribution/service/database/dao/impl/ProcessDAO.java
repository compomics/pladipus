/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.database.dao.impl;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.distribution.service.database.AutoCloseableDBConnection;
import com.compomics.pladipus.core.control.distribution.service.database.dao.PladipusDAO;
import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.processing.templates.ProcessingParameterTemplate;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessDAO extends PladipusDAO implements AutoCloseable {
/**
     * The Logging instance
     */
    public static final Logger LOGGER = Logger.getLogger(ProcessDAO.class);
    /**
     * The jobDAO instance
     */
    private static ProcessDAO pladipusJobDAO;
    /**
     * The timeout value for queries (in seconds)
     */
    private static final int queryTimeOut = 30; 

    /**
     *
     * @return the dao instance
     */
    public static ProcessDAO getInstance() {
        if (pladipusJobDAO == null) {
            pladipusJobDAO = new ProcessDAO();
        }
        return pladipusJobDAO;
    }

    private ProcessDAO() {

    }

    /**
     * The amount of errors a process has undergone accross all operations
     *
     * @param processID the id to investigate
     * @return
     * @throws SQLException
     */
    public int getStepCount(int processID) throws SQLException {
        int stepCount = -1;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement selectStepCount = c.prepareStatement("SELECT STEPCOUNT FROM PROCESS WHERE PROCESS_ID=?")) {
            selectStepCount.setInt(1, processID);
            try (ResultSet executeQuery = selectStepCount.executeQuery()) {
                if (executeQuery.next()) {
                    stepCount = executeQuery.getInt("STEPCOUNT");
                }
            }
        }
        return stepCount;
    }

    /**
     * The amount of errors a process has undergone accross all operations
     *
     * @param processID the id to investigate
     * @return
     * @throws SQLException
     */
    public int getFailCount(int processID) throws SQLException {
        int failCount = -1;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT FAILCOUNT FROM PROCESS WHERE PROCESS_ID=?")) {
            updateRun.setInt(1, processID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                if (executeQuery.next()) {
                    failCount = executeQuery.getInt("FAILCOUNT");
                }
            }
        }
        return failCount;
    }

    /**
     * Adds an instance to a failcount of a proccessID
     *
     * @param processID the failed processID
     * @throws SQLException
     */
    public void increaseFailCount(int processID) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false); PreparedStatement updateRun = c.prepareStatement("UPDATE PROCESS SET FAILCOUNT=FAILCOUNT+1,STEPCOUNT=0 WHERE PROCESS_ID=?")) {

            updateRun.setInt(1, processID);
            updateRun.executeUpdate();
            c.commit();
        }
    }

    /**
     *  Updates the process' state
     * @param processID the process to be updated
     * @param newState the new state for the process
     * @throws SQLException
     */
    public void updateProcessState(int processID, String newState) throws SQLException {
        if (newState.equalsIgnoreCase("done")) {
            setDone(processID);
        } else {
            try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                    PreparedStatement updateRun = c.prepareStatement("UPDATE PROCESS SET STATE =?,STEPCOUNT=STEPCOUNT+1 WHERE PROCESS_ID=?")) {
                updateRun.setString(1, newState);
                updateRun.setInt(2, processID);
                updateRun.executeUpdate();
                c.commit();
            }
        }
    }

    /**
     *
     * @param processID the ID of the process
     * @return the state the process was last in
     * @throws SQLException
     */
    public String getProcessState(int processID) throws SQLException {

        String state = "Unknown";
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT STATE FROM PROCESS WHERE PROCESS_ID=?")) {

            updateRun.setInt(1, processID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                if (executeQuery.next()) {
                    state = executeQuery.getString("STATE");
                }
            }
        }
        return state;
    }

    /**
     *
     * @param runID the parent run to be queried
     * @return a map of processID's with their respective states
     * @throws SQLException
     */
    public TreeMap<Integer, String> getProcessStatesForRun(int runID) throws SQLException {

        TreeMap<Integer, String> runMap = new TreeMap<>();
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT PROCESS_ID,STATE FROM PROCESS WHERE RUN_ID=?")) {

            updateRun.setInt(1, runID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                while (executeQuery.next()) {
                    runMap.put(executeQuery.getInt("PROCESS_ID"), executeQuery.getString("STATE"));
                }
            }
        }
        return runMap;
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
        return XMLJobInterpreter.getInstance().convertXMLtoJob(getXMLForProcess(processID));
    }

    /**
     *
     * @param runID
     * @return a list or processes for this parent run
     * @throws SQLException
     */
    public LinkedList<Integer> getUnqueuedProcesses() throws SQLException {

        LinkedList<Integer> processList = new LinkedList<>();
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT PROCESS_ID FROM PROCESS WHERE ON_QUEUE=?")) {

            retrieveStatement.setBoolean(1, false);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    processList.add(executeQuery.getInt("PROCESS_ID"));
                }
            }
        }
        return processList;
    }

    /**
     *
     * @param runID
     * @return a list or processes for this parent run
     * @throws SQLException
     */
    public LinkedList<Integer> getProcessesForRun(int runID) throws SQLException {

        LinkedList<Integer> processList = new LinkedList<>();
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT PROCESS_ID FROM PROCESS WHERE RUN_ID=? AND ON_QUEUE=?")) {

            retrieveStatement.setInt(1, runID);
            retrieveStatement.setBoolean(2, false);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    processList.add(executeQuery.getInt("PROCESS_ID"));
                }
            }
        }
        return processList;
    }

    /**
     *
     * @param runID
     * @return the percentage of completed processes for a particular run
     * @throws SQLException
     */
    public double getCompletedProcessesForRun(int runID) throws SQLException {

        double percentage = 0;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT COUNT(CASE COMPLETE WHEN true THEN 1 ELSE null END)/COUNT(PROCESS_ID) FROM PROCESS WHERE RUN_ID=?;")) {

            retrieveStatement.setQueryTimeout(queryTimeOut);
            retrieveStatement.setInt(1, runID);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                if (executeQuery.next()) {
                    percentage = 100 * executeQuery.getDouble(1);
                }
            }
        }
        return percentage;
    }

    /**
     *
     * @param runID
     * @param lowestLimit
     * @param upperLimit
     * @return a list or processes for this parent run
     * @throws SQLException
     */
    public LinkedList<Object[]> getProcessInformation(int runID, int lowestLimit, int pageSize) throws SQLException {
        LinkedList<Object[]> processList = new LinkedList<>();
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement retrieveStatement = c.prepareStatement("SELECT PROCESS_ID,STATE,STEPCOUNT,COMPLETE FROM PROCESS WHERE RUN_ID=? LIMIT ?,?;")) {

            retrieveStatement.setInt(1, runID);
            retrieveStatement.setInt(2, lowestLimit);
            retrieveStatement.setInt(3, pageSize);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    processList.add(new Object[]{executeQuery.getLong("PROCESS_ID"), executeQuery.getString("STATE"), executeQuery.getInt("STEPCOUNT"), executeQuery.getBoolean("COMPLETE")});
                }
            }
            return processList;
        }
    }

    /**
     *
     * @param processID
     * @return a boolean indicating if this process has been put on the queue
     * @throws SQLException
     */
    public boolean isQueued(int processID) throws SQLException {

        boolean isQueued = false;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT ON_QUEUE FROM PROCESS WHERE PROCESS_ID =?")) {

            retrieveStatement.setInt(1, processID);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                if (executeQuery.next()) {
                    isQueued = executeQuery.getBoolean("ON_QUEUE");
                }
            }
        }
        return isQueued;
    }

    /**
     *
     * @param processID
     * @return a boolean indicating if this process has been terminated (this
     * can be either due to completion or repeated failure)
     * @throws SQLException
     */
    public boolean isCompletedProcess(int processID) throws SQLException {

        boolean completed = false;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT COMPLETE FROM PROCESS WHERE PROCESS_ID=?")) {

            updateRun.setInt(1, processID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                if (executeQuery.next()) {
                    completed = executeQuery.getBoolean("COMPLETE");
                }
            }
        }
        return completed;
    }

    /**
     *
     * @param processID
     * @return the run ID of the parent of this process
     * @throws SQLException
     */
    public int getParentRunID(int processID) throws SQLException {

        int runID = -1;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT RUN.RUN_ID FROM PROCESS INNER JOIN RUN ON RUN.RUN_ID=PROCESS.RUN_ID WHERE PROCESS_ID=?")) {

            updateRun.setInt(1, processID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                if (executeQuery.next()) {
                    runID = executeQuery.getInt("RUN.RUN_ID");
                }
            }
        }
        return runID;
    }

    /**
     *
     * @param selectedProcessID a list of ids to be removed
     * @throws SQLException
     */
    public void removeProcesses(List<Integer> selectedProcessID) throws SQLException {
        String sql = selectedProcessID.toString().replace("[", "(").replace("]", ")");
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false); PreparedStatement insertProcess = c.prepareStatement("DELETE FROM PROCESS WHERE PROCESS_ID IN " + sql)) {

            insertProcess.executeUpdate();
            c.commit();
        }

    }

    /**
     *
     * @param aProcessID the process id to be handled
     * @param queued boolean indicating if it is on or off the queue
     * @throws SQLException
     */
    public void setQueued(Integer aProcessID, boolean queued) throws SQLException {

        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateRun = c.prepareStatement("UPDATE PROCESS SET STATE=?,ON_QUEUE =? WHERE PROCESS_ID=?")) {

            if (queued) {
                updateRun.setString(1, "ON QUEUE");
            } else {
                updateRun.setString(1, "AWAITING PROCESSING");
            }
            updateRun.setBoolean(2, queued);
            updateRun.setInt(3, aProcessID);
            updateRun.executeUpdate();
            c.commit();
        }
    }

    /**
     *
     * @param aProcessID the process id to be handled
     * @param queued boolean indicating if it is on or off the queue
     * @throws SQLException
     */
    public void setQueued(Collection<Integer> processIDs, boolean queued) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateRun = c.prepareStatement("UPDATE PROCESS SET STATE=?,ON_QUEUE =? WHERE PROCESS_ID=?")) {

            for (Integer aProcessID : processIDs) {
                if (queued) {
                    updateRun.setString(1, "ON QUEUE");
                } else {
                    updateRun.setString(1, "AWAITING PROCESSING");
                }
                updateRun.setBoolean(2, queued);
                updateRun.setInt(3, aProcessID);
                updateRun.addBatch();
            }
            updateRun.executeBatch();
            c.commit();
        }
    }

    /**
     *
     * @param aProcessID the processID that should be set complete
     * @throws SQLException
     */
    public void setDone(Integer aProcessID) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateRun = c.prepareStatement("UPDATE PROCESS SET COMPLETE = ? WHERE PROCESS_ID=?")) {

            updateRun.setBoolean(1, true);
            updateRun.setInt(2, aProcessID);
            updateRun.executeUpdate();
            c.commit();
        }

    }

    /**
     *
     * @param run_id the parent run
     * @return a list of processIDs that have been submitted, but have yet to be
     * put on queue. Processes that are already finished are excluded from this
     * list !
     * @throws SQLException
     */
    public LinkedList<Integer> getUnqueuedProcesses(int run_id) throws SQLException {

        LinkedList<Integer> unqueuedProcesses = new LinkedList<>();
        try (Connection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement(""
                + "SELECT PROCESS_ID FROM PROCESS WHERE (RUN_ID =? AND ON_QUEUE=0 AND COMPLETE=0)")) {
            retrieveStatement.setInt(1, run_id);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    unqueuedProcesses.add(executeQuery.getInt("PROCESS_ID"));
                }
            }
        }
        return unqueuedProcesses;
    }

    /**
     *
     * @param user the owner of the processes
     * @return a list of processIDs that have been submitted, but have yet to be
     * put on queue
     * @throws SQLException
     */
    public LinkedList<Integer> getUnqueuedProcesses(String user) throws SQLException {

        LinkedList<Integer> unqueuedProcesses = new LinkedList<>();
        try (Connection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement(""
                + "SELECT PROCESS_ID FROM PROCESS INNER JOIN RUN ON RUN.RUN_ID=PROCESS.RUN_ID WHERE (USER_NAME =? AND ON_QUEUE=?)")) {
            retrieveStatement.setString(1, user);
            retrieveStatement.setBoolean(2, false);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    unqueuedProcesses.add(executeQuery.getInt("PROCESS_ID"));
                }
            }
        }
        return unqueuedProcesses;
    }

    /**
     *
     * @param run_id the parent of the processes
     * @return a list of processIDs that have been submitted, but have yet to be
     * put on queue
     * @throws SQLException
     */
    public LinkedList<Integer> getQueuedProcesses(int run_id) throws SQLException {

        LinkedList<Integer> unqueuedProcesses = new LinkedList<>();
        try (Connection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement(""
                + "SELECT PROCESS_ID FROM PROCESS WHERE (RUN_ID=? AND ON_QUEUE=1 AND COMPLETE=0)")) {
            retrieveStatement.setInt(1, run_id);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    unqueuedProcesses.add(executeQuery.getInt("PROCESS_ID"));
                }
            }
        }
        return unqueuedProcesses;
    }

    /**
     *
     * @param template the run template for the queried process
     * @param run_id
     * @param queued
     * @param processID the process to be launched
     * @param complete
     * @return a jobXML using the template of the parent run and the parameters
     * stored in the database
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws StepLoadingException
     */
    public Collection<ProcessingJob> getJobsForRun(PladipusProcessingTemplate template, int run_id, boolean queued, boolean complete) throws SQLException, SAXException, IOException, ParserConfigurationException, StepLoadingException {
        ArrayList<ProcessingJob> processingJob = new ArrayList<>();
        HashMap<Integer, HashMap<String, String>> processParameterMap = new HashMap<>();
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement fillTemplateXMLQuery = c.prepareStatement(
                        "SELECT PROCESS.PROCESS_ID,NAME,VALUE,ON_QUEUE,COMPLETE FROM PROCESS_PARAMETERS INNER JOIN PROCESS ON PROCESS.PROCESS_ID=PROCESS_PARAMETERS.PROCESS_ID WHERE RUN_ID =?")) {

            fillTemplateXMLQuery.setInt(1, run_id);
            ResultSet parameterResultSet = fillTemplateXMLQuery.executeQuery();
            while (parameterResultSet.next() & !parameterResultSet.isClosed()) {
                if (complete == parameterResultSet.getBoolean("COMPLETE") && queued == parameterResultSet.getBoolean("ON_QUEUE")) {
                    int processID = parameterResultSet.getInt("PROCESS.PROCESS_ID");
                    String parameter_Name = parameterResultSet.getString("NAME");
                    String parameter_Value = parameterResultSet.getString("VALUE");
                    HashMap<String, String> parameterMap = processParameterMap.getOrDefault(processID, new HashMap<>());
                    parameterMap.put(parameter_Name, parameter_Value);
                    processParameterMap.put(processID, parameterMap);
                }
            }
        }

        for (Map.Entry<Integer, HashMap<String, String>> aProcessID : processParameterMap.entrySet()) {
            try {
                ProcessingJob convertXMLtoJob = XMLJobInterpreter.getInstance().convertXMLtoJob(template.toJobXML(aProcessID.getKey()));

                //don't forget run parameters !
                HashMap<String, String> parameterMap = aProcessID.getValue();
                TreeMap<String, ProcessingParameterTemplate> runParameters = template.getRunParameters();
                for (ProcessingParameterTemplate parameter : runParameters.values()) {
                    parameterMap.put(parameter.getName(), parameter.getValue());
                }
                convertXMLtoJob.setProcessingParameters(parameterMap);
                processingJob.add(convertXMLtoJob);
            } catch (Exception ex) {
                ex.printStackTrace();
                LOGGER.error(ex);
            }
        }
        return processingJob;
    }

    /**
     *
     * @param template the run template for the queried process
     * @param processID the process to be launched
     * @return a jobXML using the template of the parent run and the parameters
     * stored in the database
     * @throws SQLException
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws StepLoadingException
     */
    public String getXMLForProcess(PladipusProcessingTemplate template, int processID) throws SQLException, SAXException, IOException, ParserConfigurationException, StepLoadingException {
        String toJobXML = null;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement fillTemplateXMLQuery = c.prepareStatement("SELECT NAME,VALUE FROM PROCESS_PARAMETERS WHERE PROCESS_ID =?")) {

            fillTemplateXMLQuery.setInt(1, processID);
            if (!fillTemplateXMLQuery.isClosed()) {
                ResultSet parameterResultSet = fillTemplateXMLQuery.executeQuery();
                while (parameterResultSet.next() & !parameterResultSet.isClosed()) {
                    template.addJobParameter(new ProcessingParameterTemplate(parameterResultSet.getString("NAME"), parameterResultSet.getString("VALUE")));
                }
                toJobXML = template.toJobXML(processID);
            }
        }
        return toJobXML;
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
    public String getXMLForProcess(int processID) throws SQLException, SAXException, IOException, ParserConfigurationException, StepLoadingException {
        String toJobXML = null;
        //XML STRING
        PladipusProcessingTemplate templateXML;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement selectRun = c.prepareStatement("SELECT TEMPLATE FROM RUN "
                        + "INNER JOIN PROCESS ON RUN.RUN_ID=PROCESS.RUN_ID WHERE PROCESS_ID =?");
                PreparedStatement fillTemplateXMLQuery = c.prepareStatement("SELECT NAME,VALUE FROM PROCESS_PARAMETERS WHERE PROCESS_ID =?")) {

            //selectRun.setQueryTimeout(queryTimeOut);
            selectRun.setInt(1, processID);
            try (ResultSet executeQuery = selectRun.executeQuery()) {
                if (executeQuery.next() & !executeQuery.isClosed()) {
                    templateXML = XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(executeQuery.getString("TEMPLATE"));
                    //      fillTemplateXMLQuery.setQueryTimeout(queryTimeOut);
                    fillTemplateXMLQuery.setInt(1, processID);
                    ResultSet parameterResultSet = fillTemplateXMLQuery.executeQuery();
                    while (parameterResultSet.next() & !parameterResultSet.isClosed()) {
                        templateXML.addJobParameter(new ProcessingParameterTemplate(parameterResultSet.getString("NAME"), parameterResultSet.getString("VALUE")));
                    }
                    toJobXML = templateXML.toJobXML(processID);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return toJobXML;
    }

    /**
     *  Removes the process from the database
     * @param selectedProcessID the process ID to remove from the database
     * @throws SQLException
     */
    public void removeProcess(int selectedProcessID) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateRun = c.prepareStatement("DELETE FROM PROCESS WHERE PROCESS_ID=?")) {

            updateRun.setInt(1, selectedProcessID);
            updateRun.executeUpdate();
            c.commit();
        }
    }

    /**
     *
     * @param selectedProcessID the process Id to update
     * @param processingParameters the parameters to be updated
     * @throws SQLException
     */
    public void updateProcessParameters(int selectedProcessID, HashMap<String, String> processingParameters) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement deleteParam = c.prepareStatement("DELETE FROM PROCESS_PARAMETERS WHERE PROCESS_ID=?");
                PreparedStatement updateRun = c.prepareStatement("INSERT INTO PROCESS_PARAMETERS(PROCESS_ID,NAME,VALUE) VALUES(?,?,?)");) {
            deleteParam.setQueryTimeout(queryTimeOut);
            updateRun.setQueryTimeout(queryTimeOut);

            //delete the process parameters for this one
            deleteParam.setInt(1, selectedProcessID);
            deleteParam.executeUpdate();
            //re-add the parameters
            for (Map.Entry<String, String> aParameter : processingParameters.entrySet()) {
                updateRun.setInt(1, selectedProcessID);
                updateRun.setString(2, aParameter.getKey());
                updateRun.setString(3, aParameter.getValue());
                updateRun.addBatch();
            }
            updateRun.executeBatch();
            c.commit();
        }
    }

    /**
     *
     * @param processID the process ID to be reset
     * @throws SQLException
     */
    public void resetStepCount(int processID) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateRun = c.prepareStatement("UPDATE PROCESS SET STEPCOUNT=0,STATE='RESET' WHERE PROCESS_ID=?")) {

            updateRun.setInt(1, processID);
            updateRun.executeUpdate();
            c.commit();
        }
    }

    /**
     *
     * @param processID the process ID to be reset
     * @throws SQLException
     */
    public void resetProcess(int processID) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateRun = c.prepareStatement("UPDATE PROCESS SET FAILCOUNT=0,STEPCOUNT=0,STATE='CANCELLED' FROM PROCESS WHERE PROCESS_ID=?")) {

            updateRun.setInt(1, processID);
            updateRun.executeUpdate();
            c.commit();
        }
    }

    /**
     *
     * @param processIDs the collection of process IDs to be reset
     * @throws SQLException
     */
    public void resetProcesses(Collection<Integer> processIDs) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateRun = c.prepareStatement("UPDATE PROCESS SET FAILCOUNT=0,STEPCOUNT=0,STATE='CANCELLED' WHERE PROCESS_ID=?")) {

            for (int processID : processIDs) {
                updateRun.setInt(1, processID);
                updateRun.addBatch();
            }
            updateRun.executeBatch();
            c.commit();
        }
    }

    /**
     *
     * @param processID the process ID to create the processing template for
     * @return the processing template for this job
     * @throws SQLException
     * @throws IOException
     * @throws StepLoadingException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public PladipusProcessingTemplate getTemplate(int processID) throws SQLException, IOException, StepLoadingException, ParserConfigurationException, SAXException {
        PladipusProcessingTemplate template = null;
        try (Connection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement(""
                + "SELECT TEMPLATE FROM (PROCESS INNER JOIN RUN ON PROCESS.RUN_ID = RUN.RUN_ID) WHERE PROCESS_ID=?;")) {
            retrieveStatement.setInt(1, processID);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                if (executeQuery.next()) {
                    String xmlString = executeQuery.getString("TEMPLATE");
                    template = XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(xmlString.trim());
                }
            }
        }
        return template;
    }

}
