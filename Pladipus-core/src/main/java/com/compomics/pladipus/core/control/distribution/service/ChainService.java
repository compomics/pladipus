package com.compomics.pladipus.core.control.distribution.service;

import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ChainDAO;
import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 *
 * @author Kenneth Verheggen
 */
public class ChainService {

    /**
     * The processService instance
     */
    private static ChainService chainService;

    /**
     *
     * @return the processing service instance
     */
    public static ChainService getInstance() {
        if (chainService == null) {
            chainService = new ChainService();
        }
        return chainService;
    }

    private ChainService() {

    }

    /**
     *
     * @param processID
     * @return the next available chain ID
     * @throws SQLException
     */
    public int getNextChainId() throws SQLException {
        try (ChainDAO dao = ChainDAO.getInstance()) {
            return dao.getNextChainId();
        }
    }

    /**
     *
     * @param run_id
     * @return the chain ID for this process
     * @throws SQLException
     */
    public int getChainIDForRun(int run_id) throws SQLException {
        try (ChainDAO dao = ChainDAO.getInstance()) {
            return dao.getChainIdForRun(run_id);
        }
    }

    /**
     *
     * @param process_id
     * @return the chain ID for this process
     * @throws SQLException
     */
    public int getChainIDForProcess(int process_id) throws SQLException {
        try (ChainDAO dao = ChainDAO.getInstance()) {
            return dao.getChainIdForProcess(process_id);
        }
    }

    /**
     *
     * @param process_id
     * @return the workers (in rank) that are working on the same chain
     * @throws SQLException
     */
    public LinkedHashMap<Integer, String> getWorkersOnSameChain(int chain_id) throws SQLException {
        try (ChainDAO dao = ChainDAO.getInstance()) {
            return dao.getWorkersOnSameChain(chain_id);
        }
    }

    /**
     * @param chain_id the id of the chain
     * @return boolean indicating whether this job is ready
     * @throws java.sql.SQLException
     */
    public boolean canExecute(int chain_id, int process_id) throws SQLException {
        boolean isReady = false;
        try (ChainDAO dao = ChainDAO.getInstance()) {
            isReady = dao.canExecute(chain_id,process_id);
        }
        return isReady;
    }

    /**
     *
     * @param process_id the id of the process
     * @param worker_id the identifier of the worker
     * @return boolean indicating whether the worker is registered
     * @throws SQLException
     */
    public boolean registerMe(int process_id, String worker_id) throws SQLException {
        boolean isRegistered = false;
        try (ChainDAO dao = ChainDAO.getInstance()) {
            isRegistered = dao.registerWorker(process_id, worker_id);
        }
        return isRegistered;
    }


}
