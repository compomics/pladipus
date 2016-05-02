package com.compomics.pladipus.moff.logic.step;

import com.compomics.pladipus.moff.logic.util.MoffInstaller;
import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import javax.xml.stream.XMLStreamException;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class MoFFStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private File tempResources;
    /**
     * Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(MoFFStep.class);
    /**
     * The moff apex script file
     */
    private static final File MOFF_APEX_SCRIPT_FILE = new File(MoffInstaller.getMoFFFolder(), "moff.py");
    /**
     * The moff mbr script file
     */
    private static final File MOFF_MBR_SCRIPT_FILE = new File(MoffInstaller.getMoFFFolder(), "moff_all.py");
    /**
     * The mode in which this step will run (APEX or MBR)
     */
    private String mode = "APEX";
    private ProcessingEngine processingEngine;

    public MoFFStep() {

    }

    public void stop() {
        if (processingEngine != null) {
            processingEngine.stopProcess();
        }
    }

    @Override
    public boolean doAction() throws PladipusProcessingException {
        //start logging
        File logFolder = new File(parameters.get("--output_folder"));
        //Determine the script that needs to be used
        File MOFF_SCRIPT_FILE;
        mode = parameters.getOrDefault("mode", mode);
        if (mode.equalsIgnoreCase("APEX")) {
            MOFF_SCRIPT_FILE = MOFF_APEX_SCRIPT_FILE;
        } else {
            MOFF_SCRIPT_FILE = MOFF_MBR_SCRIPT_FILE;
        }

        LOGGER.info("Running MoFF in " + mode.toUpperCase() + " mode.");
        //check the installation
        if (!MOFF_SCRIPT_FILE.exists()) {
            LOGGER.info("Installing MoFF...");
            try {
                MoffInstaller.installMoff();
            } catch (IOException | URISyntaxException ex) {
                throw new PladipusProcessingException(ex);
            }
            //check if the installation was correct
            if (!MOFF_SCRIPT_FILE.exists()) {
                throw new PladipusProcessingException(MOFF_SCRIPT_FILE.getAbsolutePath() + " could not be found! Please check user privileges and try again.");
            }
        }
        tempResources = new File(MoffInstaller.getMoFFFolder(), "temp");
        tempResources.mkdirs();
        //convert the arguments
        List<String> constructArguments;
        try {
            constructArguments = constructArguments(MOFF_SCRIPT_FILE);
            //run the scripts
        } catch (IOException | XMLStreamException | URISyntaxException ex) {
            throw new PladipusProcessingException(ex);
        }

        CallbackNotifier callbackNotifier = getCallbackNotifier();
        //add custom error words in case something goes wrong, make sure the processing engine can discover it
        List<String> errorKeyWords = new ArrayList<>();
        errorKeyWords.add("ERROR");
        processingEngine = new ProcessingEngine();
        try {
            processingEngine.startProcess(MOFF_SCRIPT_FILE, constructArguments, callbackNotifier, errorKeyWords);
        } catch (IOException | InterruptedException | ExecutionException ex) {
            throw new PladipusProcessingException(ex);
        }
        return true;
    }

    private List<String> constructArguments(File MOFF_SCRIPT_FILE) throws IOException, XMLStreamException, URISyntaxException {
        if (!MOFF_SCRIPT_FILE.exists()) {
            MoffInstaller.installMoff();
        }
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("python");
        cmdArgs.add(MOFF_SCRIPT_FILE.getAbsolutePath());
        for (Entry<String, String> aParameter : parameters.entrySet()) {
            if (!aParameter.getKey().equalsIgnoreCase("mode")) {
                cmdArgs.add(aParameter.getKey());
                cmdArgs.add(aParameter.getValue());
            }
        }
        return cmdArgs;
    }

    @Override
    public String getDescription() {
        return "Running MoFF (" + mode + ")";
    }

}
