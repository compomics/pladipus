package com.compomics.pladipus.core.model.processing;

import java.util.LinkedHashMap;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessingRun {

    /**
     * The processes that need to be run
     */
    private final LinkedHashMap<String, ProcessingJob> processesToRun;
    /**
     * The name of this run
     */
    private final String runName;
    /**
     * The chain ID for this run (in case of sequential jobs)
     */
    private int chainID = -1;
    /**
     * Boolean indicating if the order of the run needs to be respected across
     * workers
     */
    private boolean keepOrder = false;

    public ProcessingRun(LinkedHashMap<String, ProcessingJob> processesToRun, String runName) {
        this.processesToRun = processesToRun;
        this.runName = runName;
    }

    public ProcessingRun(LinkedHashMap<String, ProcessingJob> processesToRun, String runName, boolean keepOrder) {
        this.processesToRun = processesToRun;
        this.runName = runName;
        this.keepOrder = keepOrder;
        //set all the processingjobs to check their order
        
    }

    public LinkedHashMap<String, ProcessingJob> getProcesses() {
        return processesToRun;
    }

    public String getRunName() {
        return runName;
    }

    public int getChainID() {
        return chainID;
    }

    public boolean isKeepOrder() {
        return keepOrder;
    }

}
