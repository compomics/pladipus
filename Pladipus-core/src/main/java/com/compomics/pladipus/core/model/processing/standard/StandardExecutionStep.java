/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing.standard;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.util.Arrays;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class StandardExecutionStep extends ProcessingStep {
 /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(StandardExecutionStep.class);

    @Override
    public boolean doAction() throws Exception {
        String cmd = parameters.get("cmd");
        String executable = parameters.get("executable");
        ProcessingEngine.startProcess(new File(executable), Arrays.asList(cmd.split(" ")));
        return true;
    }      
    
    @Override
    public String getDescription() {
        return "Executing command line";
    }
}
