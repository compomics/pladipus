package com.compomics.pladipus.core.model.processing.standard;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class CommandLineExecutionStep extends ProcessingStep {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CommandLineExecutionStep.class);
    private String[] args;

    @Override
    public boolean doAction() throws Exception {
        String executable = System.getProperty("user.home");
        if (parameters != null) {
            executable = parameters.getOrDefault("executable", executable);
        }
        if (args != null) {
            startProcess(new File(executable), args);
        } else {
            startProcess(new File(executable), constructArguments());
        }
        return true;
    }

    public void setCommand(String cmd) {
        this.args = cmd.split(" ");
    }

    public void SetCommand(String[] args) {
        this.args = args;
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
        return "Executing a custom command line";
    }

    public static void main(String[] args) {
        CommandLineExecutionStep commandLineExecutionStep = new CommandLineExecutionStep();
        // commandLineExecutionStep.setCommand("ping www.google.be");
        commandLineExecutionStep.setCommand("java -version");
        try {
            commandLineExecutionStep.doAction();
        } catch (Exception ex) {
            ex.printStackTrace();
            commandLineExecutionStep.close();
        }
    }

}
