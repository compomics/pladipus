package com.compomics.moff.gui.control.step;

import com.compomics.moff.gui.control.util.MoffInstaller;
import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
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

    public void stop(){
        if(processingEngine!=null){
            processingEngine.stopProcess();
        }
    }
    
    
    @Override
    public boolean doAction() throws Exception {
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
            MoffInstaller.installMoff();
            //check if the installation was correct
            if (!MOFF_SCRIPT_FILE.exists()) {
                throw new FileNotFoundException(MOFF_SCRIPT_FILE.getAbsolutePath() + " could not be found! Please check user privileges and try again.");
            }
        }
        tempResources = new File(MoffInstaller.getMoFFFolder(), "temp");
        tempResources.mkdirs();
        //convert the arguments
        List<String> constructArguments = constructArguments(MOFF_SCRIPT_FILE);
        //run the scripts

        CallbackNotifier callbackNotifier = getCallbackNotifier();
        //add custom error words in case something goes wrong, make sure the processing engine can discover it
        List<String> errorKeyWords = new ArrayList<>();
        errorKeyWords.add("ERROR");
        processingEngine = new ProcessingEngine();
               processingEngine .startProcess(MOFF_SCRIPT_FILE, constructArguments, callbackNotifier, errorKeyWords);
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
