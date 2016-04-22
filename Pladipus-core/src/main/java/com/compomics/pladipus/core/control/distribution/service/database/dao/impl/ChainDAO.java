package com.compomics.pladipus.core.control.distribution.service.database.dao.impl;

import com.compomics.pladipus.core.control.distribution.service.database.AutoCloseableDBConnection;
import com.compomics.pladipus.core.control.distribution.service.database.dao.PladipusDAO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.jdom2.IllegalTargetException;

/**
 *
 * @author Kenneth Verheggen
 */
public class ChainDAO extends PladipusDAO implements AutoCloseable {

    /**
     * The Logging instance
     */
    public static final Logger LOGGER = Logger.getLogger(ChainDAO.class);
    /**
     * The DAO instance
     */
    private static ChainDAO pladipusJobDAO;

    /**
     *
     * @return the DAO instance
     */
    public static ChainDAO getInstance() {
        if (pladipusJobDAO == null) {
            pladipusJobDAO = new ChainDAO();
        }
        return pladipusJobDAO;
    }

    private ChainDAO() {

    }

    /**
     *
     * @param process_id the process id
     * @return the chain id this process participates in
     * @throws SQLException
     */
    public int getChainIdForProcess(int process_id) throws SQLException {
        int chainID = -1;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement queryWorker = c.prepareStatement("SELECT chain_id FROM chain_activities WHERE process_id=?");) {
            queryWorker.setInt(1, process_id);
            ResultSet executeQuery = queryWorker.executeQuery();
            if (executeQuery.next()) {
                chainID = executeQuery.getInt("chain_id");
            }
        }
        return chainID;
    }

    /**
     *
     * @param run_id the process id
     * @return the chain id this process participates in
     * @throws SQLException
     */
    public int getChainIdForRun(int run_id) throws SQLException {
        int chainID = -1;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement queryWorker = c.prepareStatement("SELECT chain_id FROM chain_activities WHERE run_id=?");) {
            queryWorker.setInt(1, run_id);
            ResultSet executeQuery = queryWorker.executeQuery();
            if (executeQuery.next()) {
                chainID = executeQuery.getInt("chain_id");
            }
        }
        return chainID;
    }

    /**
     *
     * @return the next chain ID
     * @throws SQLException
     */
    public int getNextChainId() throws SQLException {
        int chainID = 0;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement queryWorker = c.prepareStatement("SELECT MAX(chain_id) FROM chain_activities");) {
            ResultSet executeQuery = queryWorker.executeQuery();
            if (executeQuery.next()) {
                chainID = executeQuery.getInt(1) + 1;
            }
        }
        return chainID;
    }

    /**
     *
     * @param worker_id
     * @return the chain a worker is participating in
     * @throws SQLException
     */
    public int getChainId(String worker_id) throws SQLException {
        int chainID = -1;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement queryWorker = c.prepareStatement("SELECT chain_id FROM chain_activities WHERE worker_id=?");) {
            queryWorker.setString(1, worker_id);
            ResultSet executeQuery = queryWorker.executeQuery();
            if (executeQuery.next()) {
                chainID = executeQuery.getInt("chain_id");
            }
        }
        return chainID;
    }

    /**
     *
     * @param process_id the process id a worker will work on
     * @param worker_id the hostname of the worker
     * @return a boolean if the registration was succesful
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public boolean registerWorker(int process_id, String worker_id) throws SQLException, IllegalArgumentException {
        boolean created = false;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement queryProcess = c.prepareStatement("SELECT chain_id FROM chain_activities WHERE process_id=?");
                PreparedStatement updateWorker = c.prepareStatement(
                        "UPDATE chain_activities SET worker_id=?, busy=1 WHERE (process_id=? AND chain_id=?)");) {

            int chain_id = -1;
            queryProcess.setInt(1, process_id);
            ResultSet executeQuery = queryProcess.executeQuery();
            if (executeQuery.next()) {
                chain_id = executeQuery.getInt("chain_id");
                updateWorker.setString(1, worker_id);
                updateWorker.setInt(2, process_id);
                updateWorker.setInt(3, chain_id);
                updateWorker.executeUpdate();
                created = true;
                c.commit();
                //    ResultSet rs = updateWorker.getGeneratedKeys();
            }
            return created;
        }
    }

    /**
     *
     * @param process_id the process id a worker will work on
     * @param worker_id the hostname of the worker
     * @return a boolean if the registration was succesful
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public boolean resetOnChain(int process_id) throws SQLException, IllegalArgumentException {
        boolean created = false;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement queryProcess = c.prepareStatement("SELECT chain_id FROM chain_activities WHERE process_id=?");
                PreparedStatement updateWorker = c.prepareStatement(
                        "UPDATE chain_activities SET worker_id=?, busy=0 WHERE (process_id=? AND chain_id=?)");) {
            int chain_id = -1;
            queryProcess.setInt(1, process_id);
            ResultSet executeQuery = queryProcess.executeQuery();
            if (executeQuery.next()) {
                chain_id = executeQuery.getInt("chain_id");

                updateWorker.setString(1, "-");
                updateWorker.setInt(2, process_id);
                updateWorker.setInt(3, chain_id);
                updateWorker.executeUpdate();
                created = true;
                c.commit();
            }
            //    ResultSet rs = updateWorker.getGeneratedKeys();
            return created;
        }
    }

    /**
     *
     * @param processIds the ids of the processes part of this chain
     * @return the created chain id
     * @throws SQLException
     */
    public int addChain(int run_id, Collection<Integer> processIds) throws SQLException {
        int nextChainId = getNextChainId();
        addChain(run_id, nextChainId, processIds);
        return nextChainId;
    }

    /**
     *
     * @param chain_id
     * @param processIds the ids of the processes part of this chain
     * @param chainID the chainID for the processing chain
     * @return the success of creating
     * @throws SQLException
     */
    public boolean addChain(int run_id, int chain_id, Collection<Integer> processIds) throws SQLException {
        boolean created;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement queryWorker = c.prepareStatement("SELECT chain_id FROM chain_activities WHERE chain_id=?");
                PreparedStatement updateWorker = c.prepareStatement("INSERT INTO chain_activities(run_id,chain_id,process_id) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);) {
            queryWorker.setInt(1, chain_id);
            if (!queryWorker.executeQuery().next()) {
                for (int aProcessId : processIds) {
                    updateWorker.setInt(1, run_id);
                    updateWorker.setInt(2, chain_id);
                    updateWorker.setInt(3, aProcessId);
                    updateWorker.addBatch();
                }
                updateWorker.executeBatch();
                //    ResultSet rs = updateWorker.getGeneratedKeys();
                created = true;
                c.commit();
            } else {
                created = false;
            }
        }
        return created;
    }

    /**
     *
     * @param workerID to be queried
     * @return boolean indicating worker is busy
     * @throws SQLException
     */
    public boolean getWorkerBusy(String workerID) throws SQLException {
        boolean busy = true;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement selectWorker = c.prepareStatement("SELECT busy FROM chain_activities WHERE worker_id=?")) {
            selectWorker.setString(1, workerID);
            try (ResultSet executeQuery = selectWorker.executeQuery()) {
                if (executeQuery.next()) {
                    busy = executeQuery.getBoolean("busy");
                }
            }
        }
        return busy;
    }

    /**
     *
     * @param chain_id the id of a chain
     * @return a map of workers working or waiting for steps
     * @throws SQLException
     */
    public LinkedHashMap<Integer, String> getWorkersOnSameChain(int chain_id) throws SQLException {
        LinkedHashMap<Integer, String> workersOnProcess = new LinkedHashMap<>();
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement selectWorker = c.prepareStatement("SELECT process_id,worker_id FROM chain_activities WHERE chain_id=?")) {
            selectWorker.setInt(1, chain_id);
            try (ResultSet executeQuery = selectWorker.executeQuery()) {
                while (executeQuery.next()) {
                    workersOnProcess.put(executeQuery.getInt("process_id"), executeQuery.getString("worker_id"));
                }
            }
        }
        return workersOnProcess;
    }

    /**
     * checks if a particular chained task is ready for processing
     *
     * @param chain_id the id this process is part off order
     * @param process_id the process_id
     * @return
     * @throws java.sql.SQLException
     */
    public boolean canExecute(int chain_id, int process_id) throws SQLException {
        boolean isReady = false;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement getChain = c.prepareStatement("SELECT process_id,busy FROM chain_activities WHERE (chain_id=?)")) {
            getChain.setInt(1, chain_id);
            LinkedHashMap<Integer, Boolean> clearedToRunMap = new LinkedHashMap<>();
            try (ResultSet executeQuery = getChain.executeQuery()) {
                while (executeQuery.next()) {
                    clearedToRunMap.put(executeQuery.getInt("process_id"), executeQuery.getBoolean("busy"));
                }
            }
            int predecessingProcess = -1;
            for (Entry<Integer, Boolean> aProcess : clearedToRunMap.entrySet()) {
                if (aProcess.getKey() == process_id) {
                    break;
                } else {
                    predecessingProcess = aProcess.getKey();
                    //check if this process is already busy
                }
            }
            if (predecessingProcess == -1) {
                //then there's no predecessing process, meaning it can be run for sure
                isReady = true;
            } else if (clearedToRunMap.get(predecessingProcess)) {
                try ( //then it's busy, but it can be that it's not yet done !
                        ProcessDAO pdao = ProcessDAO.getInstance()) {
                    isReady = pdao.isCompletedProcess(process_id);
                }
            }
        }
        return isReady;
    }

}
