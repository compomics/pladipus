package com.compomics.pladipus.moff.steps;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.enums.AllowedMoffParams;
import com.compomics.pladipus.core.model.enums.AllowedSearchGUIParams;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.moff.logic.MoffInstaller;
import com.compomics.pladipus.moff.logic.PSOutputParser;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class MoffStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources;
    private static final Logger LOGGER = Logger.getLogger(MoffStep.class);
    private static final File moFFScriptFile = new File(System.getProperty("user.home") + "/pladipus/tools/moff/moff_all.py");
    private final File rawResources;
    private final File psResources;

    public MoffStep() {
        tempResources = new File(System.getProperty("user.home") + "/pladipus/tools/moff/");
        rawResources = new File(tempResources, "RAW");
        psResources = new File(tempResources, "peptideshaker");
        tempResources.getParentFile().mkdirs();
    }

    @Override
    public boolean doAction() throws Exception {
        cleanup();
        String[] raw_input_paths = parameters.get("raw").split(",");
        String[] peptideshaker_output_paths = parameters.get("ps_report").split(",");
        for (String aPeptideShakerPath : peptideshaker_output_paths) {
            //convert the ps_output
            File temp = PladipusFileDownloadingService.downloadFile(aPeptideShakerPath, psResources);
            File ps_output_file = new File(temp.getAbsolutePath()+System.currentTimeMillis());
            PSOutputParser.convert(temp, ps_output_file);
            temp.delete();
            ps_output_file.renameTo(temp);
        }
        for (String aRawInputPath : raw_input_paths) {
            File raw_input_File = PladipusFileDownloadingService.downloadFile(aRawInputPath, rawResources);
        }
        //convert the arguments
        List<String> constructArguments = constructArguments();
        //run the scripts
        CallbackNotifier callbackNotifier = getCallbackNotifier();
        new ProcessingEngine().startProcess(moFFScriptFile, constructArguments, callbackNotifier);
        return true;
    }

    private void cleanup() throws IOException {
        if (!parameters.containsKey("skip_cleaning") && tempResources.exists()) {
            for (File aFile : tempResources.listFiles()) {
                if (aFile.exists()) {
                    if (aFile.isFile()) {
                        aFile.delete();
                    } else {
                        FileUtils.deleteDirectory(aFile);
                    }
                }
            }
        } else {
            tempResources.mkdirs();
        }
    }

    private List<String> constructArguments() throws IOException, XMLStreamException, URISyntaxException {
        MoffInstaller.installMoff();
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("python");
        cmdArgs.add(moFFScriptFile.getAbsolutePath());
        parameters.put("-inputF",psResources.getAbsolutePath());
        parameters.put("-raw_rep",rawResources.getAbsolutePath());
        cmdArgs.add(rawResources.getAbsolutePath());
        for (AllowedMoffParams aParameter : AllowedMoffParams.values()) {
            if (parameters.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(parameters.get(aParameter.getId()));
            } else if (aParameter.isMandatory()) {
                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
            }
        }
        return cmdArgs;
    }

    @Override
    public String getDescription() {
        return "Initialisation of the moFF process";
    }

}
