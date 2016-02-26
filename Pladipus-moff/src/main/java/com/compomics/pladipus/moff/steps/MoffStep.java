package com.compomics.pladipus.moff.steps;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.enums.AllowedMoffParams;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.moff.logic.MoffInstaller;
import com.compomics.pladipus.moff.logic.PSOutputParser;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private File tempResources;
    private static final Logger LOGGER = Logger.getLogger(MoffStep.class);
    private static final File moFFScriptFile = new File(System.getProperty("user.home") + "/pladipus/tools/moff/moff.py");
    private File rawResources;
    private File psResources;
    private File inputPeptideShakerFile;

    public MoffStep() {

    }

    private File findPsFiles(File rawFile) throws IOException {
        String[] peptideshaker_output_paths;
        ArrayList<String> peptideShakerFiles = new ArrayList<>();
        if (!parameters.containsKey("ps_report")) {

            //check if peptideshaker was run before...
            File temp_peptideshaker_output = new File(parameters.get("output_folder"), "reports");
            LOGGER.info("Looking for peptide shaker output in " + temp_peptideshaker_output.getAbsolutePath());

            File[] listFiles = temp_peptideshaker_output.listFiles();

            int i = 0;
            for (File anInput : listFiles) {
                if (anInput.getName().toLowerCase().contains("extended_psm_report")) {
                    peptideShakerFiles.add(anInput.getAbsolutePath());
                }
            }
        } else {
            peptideshaker_output_paths = parameters.get("ps_report").split(",");
            peptideShakerFiles.addAll(Arrays.asList(peptideshaker_output_paths));
        }
        //ToDo work for one file only
        //for (String psOutputFile : peptideShakerFiles) {
        //convert the ps_output
        String psOutputFile = peptideShakerFiles.get(0);
        LOGGER.info("Found PS-output : " + psOutputFile);
        File temp = PladipusFileDownloadingService.downloadFile(psOutputFile, psResources);
        File ps_output_file = new File(psResources, rawFile.getName().toLowerCase().replace(".raw", ".txt"));
        PSOutputParser.convert(temp, ps_output_file);
        temp.delete();
        //}
        return ps_output_file;
    }

    @Override
    public boolean doAction() throws Exception {
        tempResources = new File(System.getProperty("user.home") + "/pladipus/tools/moff/");
        //cleanup();
        tempResources.mkdirs();
        
        rawResources = new File(tempResources, "RAW");
        if (rawResources.exists()) {
            FileUtils.deleteDirectory(rawResources);
        } 
            rawResources.mkdirs();
        
        psResources = new File(tempResources, "peptideshaker");
        if (psResources.exists()) {
            FileUtils.deleteDirectory(psResources);
        }   psResources.mkdirs();
        
        for (Map.Entry<String, String> aParam : parameters.entrySet()) {
            System.out.println(aParam.getKey() + "\t" + aParam.getValue());
        }
        //fibd raw files
        //ToDo this should become work for a single file
        String[] raw_input_paths = parameters.get("raw").split(",");
        // for (String aRawInputPath : raw_input_paths) {
        File raw_input_File = PladipusFileDownloadingService.downloadFile(raw_input_paths[0], rawResources);
        //find peptideshaker paths
        inputPeptideShakerFile = findPsFiles(raw_input_File);
        //convert the arguments
        List<String> constructArguments = constructArguments();
        //run the scripts
        CallbackNotifier callbackNotifier = getCallbackNotifier();

        new ProcessingEngine()
                .startProcess(moFFScriptFile, constructArguments, callbackNotifier);

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
        if (!moFFScriptFile.exists()) {
            MoffInstaller.installMoff();
        }
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("python");
        cmdArgs.add(moFFScriptFile.getAbsolutePath());
        cmdArgs.add("--input");
        cmdArgs.add(inputPeptideShakerFile.getAbsolutePath());
        cmdArgs.add("--tol");
        cmdArgs.add("10");
        cmdArgs.add("--rt_w");
        cmdArgs.add("3");
        cmdArgs.add("--rt_p");
        cmdArgs.add("0.10");
        cmdArgs.add("--raw_repo");
        //add a forward slash
        cmdArgs.add(rawResources.getAbsolutePath()+"/");
        cmdArgs.add("--output_folder");
        cmdArgs.add(parameters.get("output_folder"));

        parameters.put("-raw_repo", rawResources.getAbsolutePath());

        /*
        //fix the double dash
        if (parameters.containsKey("tol")) {
            parameters.put("-tol", parameters.get("tol"));
            //default to 10ppm
        } else if (!parameters.containsKey("-tol")) {
            parameters.put("-tol", "10");
        }
       
        for (AllowedMoffParams aParameter : AllowedMoffParams.values()) {
            LOGGER.info(aParameter);
            if (parameters.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(parameters.get(aParameter.getId()));
            } else if (aParameter.isMandatory()) {
                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
            } else {
                //         LOGGER.info("Unknown and skipped parameter : " + aParameter.getId());
            }
        }
         */
        return cmdArgs;
    }

    @Override
    public String getDescription() {
        return "Initialisation of the moFF process";
    }

}
