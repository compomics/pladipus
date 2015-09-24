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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement selectStepCount = c.prepareStatement("SELECT stepcount FROM process WHERE process_id=?")) {
            selectStepCount.setInt(1, processID);
            try (ResultSet executeQuery = selectStepCount.executeQuery()) {
                if (executeQuery.next()) {
                    stepCount = executeQuery.getInt("stepcount");
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT failcount FROM PROCESS WHERE process_id=?")) {
            updateRun.setInt(1, processID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                if (executeQuery.next()) {
                    failCount = executeQuery.getInt("failcount");
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false); PreparedStatement updateRun = c.prepareStatement("UPDATE process SET failcount=failcount+1,stepcount=0 WHERE process_id=?")) {

            updateRun.setInt(1, processID);
            updateRun.executeUpdate();
            c.commit();
        }
    }

    /**
     * Updates the process' state
     *
     * @param processID the process to be updated
     * @param newState the new state for the process
     * @throws SQLException
     */
    public void updateProcessState(int processID, String newState) throws SQLException {
        if (newState.equalsIgnoreCase("done")) {
            setDone(processID);
        } else {
            try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                    PreparedStatement updateRun = c.prepareStatement("UPDATE process SET state =? WHERE process_id=?")) {
                updateRun.setString(1, newState);
                updateRun.setInt(2, processID);
                updateRun.executeUpdate();
                c.commit();
            }
        }
    }

    public void updateProcessCounter(int processID) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateRun = c.prepareStatement("UPDATE process SET stepcount=stepcount+1 WHERE process_id=?")) {
            updateRun.setInt(1, processID);
            updateRun.executeUpdate();
            c.commit();
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT state FROM process WHERE process_id=?")) {

            updateRun.setInt(1, processID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                if (executeQuery.next()) {
                    state = executeQuery.getString("state");
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT process_id,state FROM process WHERE run_id=?")) {

            updateRun.setInt(1, runID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                while (executeQuery.next()) {
                    runMap.put(executeQuery.getInt("process_id"), executeQuery.getString("state"));
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT process_id FROM process WHERE on_queue=?")) {

            retrieveStatement.setBoolean(1, false);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    processList.add(executeQuery.getInt("process_id"));
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT process_id FROM PROCESS WHERE run_id=? AND on_queue=?")) {

            retrieveStatement.setInt(1, runID);
            retrieveStatement.setBoolean(2, false);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    processList.add(executeQuery.getInt("process_id"));
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT COUNT(CASE complete WHEN true THEN 1 ELSE null END)/COUNT(process_id) FROM process WHERE run_id=?;")) {

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
                PreparedStatement retrieveStatement = c.prepareStatement("SELECT process_id,state,stepcount,complete FROM process WHERE run_id=? LIMIT ?,?;")) {

            retrieveStatement.setInt(1, runID);
            retrieveStatement.setInt(2, lowestLimit);
            retrieveStatement.setInt(3, pageSize);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    processList.add(new Object[]{executeQuery.getLong("process_id"), executeQuery.getString("state"), executeQuery.getInt("stepcount"), executeQuery.getBoolean("complete")});
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT on_queue FROM process WHERE process_id =?")) {

            retrieveStatement.setInt(1, processID);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                if (executeQuery.next()) {
                    isQueued = executeQuery.getBoolean("on_queue");
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT complete FROM process WHERE process_id=?")) {

            updateRun.setInt(1, processID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                if (executeQuery.next()) {
                    completed = executeQuery.getBoolean("complete");
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT run.run_id FROM process INNER JOIN run ON run.run_id=process.run_id WHERE process_id=?")) {

            updateRun.setInt(1, processID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                if (executeQuery.next()) {
                    runID = executeQuery.getInt("run.run_id");
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
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false); PreparedStatement insertProcess = c.prepareStatement("DELETE FROM process WHERE process_id IN " + sql)) {

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
                PreparedStatement updateRun = c.prepareStatement("UPDATE process SET state=?,on_queue =? WHERE process_id=?")) {

            if (queued) {
                updateRun.setString(1, "Waiting to be pulled by worker ...");
            } else {
                updateRun.setString(1, "Waiting to be dispatched ...");
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
                PreparedStatement updateRun = c.prepareStatement("UPDATE process SET state=?,on_queue =? WHERE process_id=?")) {

            for (Integer aProcessID : processIDs) {
                if (queued) {
                    updateRun.setString(1, "Waiting to be pulled by worker ...");
                } else {
                    updateRun.setString(1, "Waiting to be dispatched ...");
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
                PreparedStatement updateRun = c.prepareStatement("UPDATE process SET complete = ? WHERE process_id=?")) {

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
                + "SELECT process_id FROM process WHERE (run_id =? AND on_queue=0 AND complete=0)")) {
            retrieveStatement.setInt(1, run_id);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    unqueuedProcesses.add(executeQuery.getInt("process_id"));
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
                + "SELECT process_id FROM process INNER JOIN RUN ON run.run_id=process.run_id WHERE (user_name =? AND on_queue=?)")) {
            retrieveStatement.setString(1, user);
            retrieveStatement.setBoolean(2, false);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    unqueuedProcesses.add(executeQuery.getInt("process_id"));
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
                + "SELECT process_id FROM process WHERE (run_id=? AND on_queue=1 AND complete=0)")) {
            retrieveStatement.setInt(1, run_id);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    unqueuedProcesses.add(executeQuery.getInt("process_id"));
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
                        "SELECT process.process_id,name,value,on_queue,complete FROM process_parameters INNER JOIN process ON process.process_id=process_parameters.process_id WHERE run_id =?")) {

            fillTemplateXMLQuery.setInt(1, run_id);
            ResultSet parameterResultSet = fillTemplateXMLQuery.executeQuery();
            while (parameterResultSet.next() & !parameterResultSet.isClosed()) {
                if (complete == parameterResultSet.getBoolean("complete") && queued == parameterResultSet.getBoolean("on_queue")) {
                    int processID = parameterResultSet.getInt("process.process_id");
                    String parameter_Name = parameterResultSet.getString("name");
                    String parameter_Value = parameterResultSet.getString("value");
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
                PreparedStatement fillTemplateXMLQuery = c.prepareStatement("SELECT name,value FROM process_parameters WHERE process_id=?")) {

            fillTemplateXMLQuery.setInt(1, processID);
            if (!fillTemplateXMLQuery.isClosed()) {
                ResultSet parameterResultSet = fillTemplateXMLQuery.executeQuery();
                while (parameterResultSet.next() & !parameterResultSet.isClosed()) {
                    template.addJobParameter(new ProcessingParameterTemplate(parameterResultSet.getString("name"), parameterResultSet.getString("value")));
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
                PreparedStatement selectRun = c.prepareStatement("SELECT template FROM run "
                        + "INNER JOIN process ON run.run_id=process.run_id WHERE process_id =?");
                PreparedStatement fillTemplateXMLQuery = c.prepareStatement("SELECT name,value FROM process_parameters WHERE process_id=?")) {

            //selectRun.setQueryTimeout(queryTimeOut);
            selectRun.setInt(1, processID);
            try (ResultSet executeQuery = selectRun.executeQuery()) {
                if (executeQuery.next() & !executeQuery.isClosed()) {
                    templateXML = XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(executeQuery.getString("template"));
                    //      fillTemplateXMLQuery.setQueryTimeout(queryTimeOut);
                    fillTemplateXMLQuery.setInt(1, processID);
                    ResultSet parameterResultSet = fillTemplateXMLQuery.executeQuery();
                    while (parameterResultSet.next() & !parameterResultSet.isClosed()) {
                        templateXML.addJobParameter(new ProcessingParameterTemplate(parameterResultSet.getString("name"), parameterResultSet.getString("value")));
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
     * Removes the process from the database
     *
     * @param selectedProcessID the process ID to remove from the database
     * @throws SQLException
     */
    public void removeProcess(int selectedProcessID) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateRun = c.prepareStatement("DELETE FROM process WHERE process_id=?")) {

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
                PreparedStatement deleteParam = c.prepareStatement("DELETE FROM process_parameteters process_id PROCESS_ID=?");
                PreparedStatement updateRun = c.prepareStatement("INSERT INTO process_parameteters(process_id,name,value) VALUES(?,?,?)");) {
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
                PreparedStatement updateRun = c.prepareStatement("UPDATE process SET stepcount=0,state='Resetting...' WHERE process_id=?")) {
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
                PreparedStatement updateRun = c.prepareStatement("UPDATE process SET failcount=0,stepcount=0,state='Cancelled' FROM process WHERE process_id=?")) {

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
                PreparedStatement updateRun = c.prepareStatement("UPDATE process SET failcount=0,stepcount=0,state='Cancelled' WHERE process_id=?")) {

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
                + "SELECT template FROM (process INNER JOIN run ON process.run_id = run.run_id) WHERE process_id=?;")) {
            retrieveStatement.setInt(1, processID);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                if (executeQuery.next()) {
                    String xmlString = executeQuery.getString("template");
                    template = XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(xmlString.trim());
                }
            }
        }
        return template;
    }

}
