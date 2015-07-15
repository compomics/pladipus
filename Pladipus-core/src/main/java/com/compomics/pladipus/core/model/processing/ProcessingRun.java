/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

    public ProcessingRun(LinkedHashMap<String, ProcessingJob> processesToRun, String runName) {
        this.processesToRun = processesToRun;
        this.runName = runName;
    }

    public LinkedHashMap<String, ProcessingJob> getProcesses() {
        return processesToRun;
    }

    public String getRunName() {
        return runName;
    }

}
