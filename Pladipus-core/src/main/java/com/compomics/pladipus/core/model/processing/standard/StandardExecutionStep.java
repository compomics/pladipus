package com.compomics.pladipus.core.model.processing.standard;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
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
        String executable = parameters.get("executable");
        startProcess(new File(executable), constructArguments());
        return true;
    }

    private List<String> constructArguments() {
        List<String> cmd = new ArrayList<>();
        for (Entry<String, String> keyValue : parameters.entrySet()) {
            if (!keyValue.getKey().equalsIgnoreCase("executable")) {
                cmd.add(keyValue.getKey());
                cmd.add(keyValue.getValue());
            }
        }
        return cmd;
    }

    @Override
    public String getDescription() {
        return "Executing command line";
    }
}
