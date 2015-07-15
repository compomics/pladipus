/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.database.dao.impl;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.distribution.service.database.AutoCloseableDBConnection;
import com.compomics.pladipus.core.control.distribution.service.database.dao.PladipusDAO;
import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
 * Contacts and manages the RUN information in the database
 *
 * @author Kenneth Verheggen
 */
public class RunDAO extends PladipusDAO implements AutoCloseable {

    /**
     * The Logging instance
     */
    public static final Logger LOGGER = Logger.getLogger(RunDAO.class);
    /**
     * The DAO Instance
     */
    private static RunDAO pladipusJobDAO;

    public static RunDAO getInstance() {
        if (pladipusJobDAO == null) {
            pladipusJobDAO = new RunDAO();
        }
        return pladipusJobDAO;
    }

    private RunDAO() {

    }

    /**
     *
     * @param runID
     * @return the owner of this run's mailing address
     * @throws SQLException
     */
    public String getOwnerContact(int runID) throws SQLException {

        String contactAdress = "";
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT CONTACT FROM RUN INNER JOIN USERS ON RUN.USER_NAME=USERS.USER_NAME WHERE RUN_ID=?")) {

            retrieveStatement.setInt(1, runID);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    contactAdress = executeQuery.getString("CONTACT");
                }
            }
        }
        return contactAdress;
    }

    /**
     *
     * @param user the owner of the runs
     * @return a map containing the run_Ids and their titles
     * @throws SQLException
     */
    public TreeMap<Integer, String> getRuns(String user) throws SQLException {

        TreeMap<Integer, String> runs = new TreeMap<>();
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT RUN_ID,TITLE FROM RUN WHERE USER_NAME=?")) {

            retrieveStatement.setString(1, user);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                while (executeQuery.next()) {
                    runs.put(executeQuery.getInt("RUN_ID"), executeQuery.getString("TITLE"));
                }
            }
        }
        return runs;
    }

    // processing related methods
    /**
     *
     *
     * @param template the template that will be used to launch tasks
     * @return the newly created run ID
     * @throws SQLException
     */
    public int createRun(PladipusProcessingTemplate template) throws SQLException {
        int insertedRunID;
        //step 1 = generate a run and get the ID
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false); PreparedStatement updateRun = c.prepareStatement("INSERT INTO RUN(TITLE,USER_NAME,TEMPLATE) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {

            updateRun.setString(1, template.getName());
            updateRun.setString(2, template.getUser());
            updateRun.setString(3, template.toXML());
            updateRun.executeUpdate();
            ResultSet generatedKeys = updateRun.getGeneratedKeys();
            generatedKeys.next();
            insertedRunID = generatedKeys.getInt(1);
            c.commit();
        }
        return insertedRunID;
    }

    /**
     *
     * @param runName the title of the run
     * @param userName the owner of the run
     * @return the ID corresponding to the provided information
     * @throws SQLException
     */
    public Integer getRunID(String runName, String userName) throws SQLException {
        Integer runID = -1;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement query = c.prepareStatement("SELECT RUN_ID FROM RUN WHERE TITLE=? AND USER_NAME=?")) {

            query.setString(1, runName);
            query.setString(2, userName);
            try (ResultSet executeQuery = query.executeQuery()) {
                if (executeQuery.next()) {
                    runID = executeQuery.getInt("RUN_ID");
                }
            }
        }
        return runID;
    }

    /**
     *
     * @param runID the ID where current jobs should be added to
     * @param parameterList the parameters that should be used to create the new
     * jobs
     * @return the correctly inserted processes
     * @throws SQLException
     */
    public LinkedList<Integer> addToRun(int runID, List<HashMap<String, String>> parameterList) throws SQLException {
        //step 2 = insert the processingJOB xml, update to the correct ID when retrieving from db is faster
        LinkedList<Integer> processIDs = new LinkedList<>();
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false); PreparedStatement insertProcess = c.prepareStatement("INSERT INTO PROCESS(STATE,RUN_ID,FAILCOUNT) VALUES('AWAITING EXECUTION',?,?)",
                Statement.RETURN_GENERATED_KEYS); PreparedStatement insertParameter = c.prepareStatement("INSERT INTO PROCESS_PARAMETERS(PROCESS_ID,NAME,VALUE) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {

            for (HashMap<String, String> parameterMap : parameterList) {
                insertProcess.setInt(1, runID);
                insertProcess.setInt(2, 0);
                insertProcess.executeUpdate();
                ResultSet rs = insertProcess.getGeneratedKeys();
                if (rs.next()) {
                    LOGGER.debug("Added project ID nr " + rs.getInt(1));
                    for (Map.Entry<String, String> aParameter : parameterMap.entrySet()) {
                        insertParameter.setInt(1, rs.getInt(1));
                        insertParameter.setString(2, aParameter.getKey());
                        insertParameter.setString(3, aParameter.getValue());
                        insertParameter.addBatch();
                    }
                }
                //save the generated process key
                ResultSet generatedKeys = insertProcess.getGeneratedKeys();
                if (generatedKeys.next()) {
                    processIDs.add(generatedKeys.getInt(1));
                }
            }
            LOGGER.info("Storing parameters as batch in the task database");

            insertParameter.executeBatch();
            c.commit();
        }
        return processIDs;
    }

    /**
     *
     * @param runID the ID where current jobs should be added to
     * @param parameterList the parameters that should be used to create the new
     * jobs
     * @return the correctly inserted processes
     * @throws SQLException
     */
    public int addToRun(int runID, HashMap<String, String> parameterMap) throws SQLException {
        //step 2 = insert the processingJOB xml, update to the correct ID when retrieving from db is faster
        int processID = -1;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false); PreparedStatement insertProcess = c.prepareStatement("INSERT INTO PROCESS(STATE,RUN_ID,FAILCOUNT) VALUES('AWAITING EXECUTION',?,?)",
                Statement.RETURN_GENERATED_KEYS); PreparedStatement insertParameter = c.prepareStatement("INSERT INTO PROCESS_PARAMETERS(PROCESS_ID,NAME,VALUE) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            insertProcess.setInt(1, runID);
            insertProcess.setInt(2, 0);
            insertProcess.executeUpdate();
            ResultSet rs = insertProcess.getGeneratedKeys();
            if (rs.next()) {
                LOGGER.debug("Added project ID nr " + rs.getInt(1));
                for (Map.Entry<String, String> aParameter : parameterMap.entrySet()) {
                    insertParameter.setInt(1, rs.getInt(1));
                    insertParameter.setString(2, aParameter.getKey());
                    insertParameter.setString(3, aParameter.getValue());
                    insertParameter.addBatch();
                }
            }
            //save the generated process key
            ResultSet generatedKeys = insertProcess.getGeneratedKeys();
            if (generatedKeys.next()) {
                processID = generatedKeys.getInt(1);
            }
            LOGGER.info("Storing parameters as batch in the task database");
            insertParameter.executeBatch();
            c.commit();
        }
        return processID;
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
     * @throws StepLoadingException
     */
    public PladipusProcessingTemplate getTemplateForRun(int runID) throws SQLException, IOException, StepLoadingException, ParserConfigurationException, SAXException {

        //XML STRING
        PladipusProcessingTemplate templateXML = null;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT TEMPLATE FROM RUN WHERE RUN_ID =?")) {

            updateRun.setInt(1, runID);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                while (executeQuery.next()) {
                    templateXML = XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(executeQuery.getString("TEMPLATE"));
                }
            }
        }
        return templateXML;
    }

    /**
     *
     * @param runID
     * @return a boolean indicating the completion state of this run
     * @throws SQLException
     */
    public boolean isCompletedRun(int runID) throws SQLException {

        int incomplete = 0;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement updateRun = c.prepareStatement("SELECT COUNT(COMPLETE) AS COUNTER FROM PROCESS INNER JOIN RUN ON RUN.RUN_ID=PROCESS.RUN_ID WHERE RUN.RUN_ID=? AND COMPLETE=?")) {

            updateRun.setInt(1, runID);
            updateRun.setBoolean(2, false);
            try (ResultSet executeQuery = updateRun.executeQuery()) {
                if (executeQuery.next()) {
                    incomplete = executeQuery.getInt("COUNTER");
                }
            }
        }
        return incomplete == 0;
    }

    /**
     *
     * @param run the title of the run
     * @return the owner of the run
     * @throws SQLException
     */
    @Deprecated

    public String getRunOwner(String run) throws SQLException {
        String owner = "";
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement retrieveStatement = c.prepareStatement("SELECT USER FROM RUN WHERE TITLE=?")) {

            retrieveStatement.setString(1, run);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                if (executeQuery.next()) {
                    owner = executeQuery.getString("USER");
                }
            }
        }
        return owner;
    }

    /**
     *
     * @param runID
     * @return the owner of said run
     * @throws SQLException
     */
    public String getRunOwner(int runID) throws SQLException {

        String owner = "";
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement retrieveStatement = c.prepareStatement("SELECT USER_NAME FROM RUN WHERE RUN_ID=?")) {

            retrieveStatement.setInt(1, runID);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                if (executeQuery.next()) {
                    owner = executeQuery.getString("USER_NAME");
                }
            }
        }
        return owner;
    }

    /**
     *
     * @param runID
     * @return the title of said run
     * @throws SQLException
     */
    public String getRunTitle(int runID) throws SQLException {

        String owner = "";
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement retrieveStatement = c.prepareStatement("SELECT TITLE FROM RUN WHERE RUN_ID=?")) {

            retrieveStatement.setInt(1, runID);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                if (executeQuery.next()) {
                    owner = executeQuery.getString("TITLE");
                }
            }
        }
        return owner;
    }

    /**
     *
     * @param runIDs the collection of run ids to be deleted!
     * @throws SQLException
     */
    public void deleteRun(Collection<Integer> runIDs) throws SQLException {
        for (int runID : runIDs) {
            try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                    PreparedStatement deleteRunStatement = c.prepareStatement("DELETE FROM RUN WHERE RUN_ID=?");
                    PreparedStatement deleteAssociatedProcessesStatement = c.prepareStatement("DELETE FROM PROCESS WHERE RUN_ID=?")) {

                deleteRunStatement.setInt(1, runID);
                deleteRunStatement.executeUpdate();
                deleteAssociatedProcessesStatement.setInt(1, runID);
                deleteAssociatedProcessesStatement.executeUpdate();
            }
        }
    }

    /**
     * Deletes everything from the database except user information...
     *
     * @throws SQLException
     */
    public void truncatePladipus() throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                Statement statement = c.createStatement();) {
            statement.executeUpdate("TRUNCATE RUN");
            statement.executeUpdate("TRUNCATE PROCESS");
            statement.executeUpdate("TRUNCATE PROCESS_PARAMETERS");
        }
    }

    /**
     *
     * @param runID
     * @return the size of the run
     * @throws SQLException
     */
    public int getRunSize(int runID) throws SQLException {
        int runSize = 0;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement retrieveStatement = c.prepareStatement("SELECT COUNT(RUN_ID) AS RUN_SIZE FROM PROCESS WHERE RUN_ID=?")) {

            retrieveStatement.setInt(1, runID);
            try (ResultSet executeQuery = retrieveStatement.executeQuery()) {
                if (executeQuery.next()) {
                    runSize = executeQuery.getInt("RUN_SIZE");
                }
            }
        }
        return runSize;
    }
}
