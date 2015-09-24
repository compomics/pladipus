/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.runtime.diagnostics.memory.MemoryWarningSystem;
import com.compomics.pladipus.core.control.util.JarLookupService;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.enums.AllowedSearchGUIParams;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.search.checkpoints.SearchGUICheckpoints;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class SearchGUIStep extends ProcessingStep {

    private static final Logger LOGGER = Logger.getLogger(SearchGUIStep.class);
    private static final File temp_searchGUI_output = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/SearchGUI/result");

    public SearchGUIStep() {

    }

    private List<String> constructArguments() throws IOException {
        File searchGuiJar = getJar();
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
        cmdArgs.add("-cp");
        cmdArgs.add(searchGuiJar.getAbsolutePath());
        cmdArgs.add("eu.isas.searchgui.cmd.SearchCLI");
        for (AllowedSearchGUIParams aParameter : AllowedSearchGUIParams.values()) {
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
    public boolean doAction() throws Exception, Exception {
        LOGGER.info("Running " + this.getClass().getName());
        File parameterFile = new File(parameters.get("id_params"));
        File fastaFile = new File(parameters.get("fasta_file"));
        File real_outputFolder = new File(parameters.get("output_folder"));
        //update the fasta
        LOGGER.info("Updating parameters...");
        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(parameterFile);
        identificationParameters.setFastaFile(fastaFile);
        //fix the location
        identificationParameters.setParametersFile(parameterFile);
        SearchParameters.saveIdentificationParameters(identificationParameters, parameterFile);

        if (temp_searchGUI_output.exists()) {
            temp_searchGUI_output.delete();
        }
        temp_searchGUI_output.mkdirs();

        LOGGER.info("Starting SearchGUI...");
        //use this variable if you'd run peptideshaker following this classs
        parameters.put("output_folder", temp_searchGUI_output.getAbsolutePath());
        //add callback notifier for more detailed printouts of the processing
        CallbackNotifier callbackNotifier = getCallbackNotifier();
        for (SearchGUICheckpoints aCheckPoint : SearchGUICheckpoints.values()) {
            callbackNotifier.addCheckpoint(new Checkpoint(aCheckPoint.getLine(),aCheckPoint.getFeedback()));
        }
        new ProcessingEngine().startProcess(getJar(), constructArguments(),callbackNotifier);
        //storing intermediate results
        LOGGER.info("Storing results in " + real_outputFolder);
           real_outputFolder.mkdirs();
           File outputFile = new File(real_outputFolder,"searchgui_out.zip");
           File tempOutput= new File(temp_searchGUI_output,"searchgui_out.zip");
        //copy as a stream?
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        try (FileChannel source = new FileInputStream(tempOutput).getChannel();
                FileChannel destination = new FileOutputStream(outputFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
      //  FileUtils.copyDirectory(temp_searchGUI_output, real_outputFolder);
        //in case of future peptideShaker searches : 
        parameters.put("identification_files", temp_searchGUI_output.getAbsolutePath());
        parameters.put("out",real_outputFolder.getAbsolutePath()+"/"+parameterFile.getName()+".cps");
        return true;
    }

    public File getJar() throws IOException {
        //check if this is possible in another way...
        File toolFolder = new File(System.getProperties().getProperty("user.home") + "/.compomics/pladipus/tools");
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        File temp = new File(toolFolder, "SearchGUI");
        if (!temp.exists()) {
            File searchGUIFile = PladipusFileDownloadingService.downloadFile(parameters.get("SearchGUI"), toolFolder);
            if (searchGUIFile.getName().endsWith(".zip")) {
                ZipUtils.unzipArchive(searchGUIFile, temp);
            }
        }
        return JarLookupService.lookupFile("SearchGUI-.*.jar", temp);
    }

    public boolean aVersionExistsLocal() {
        //TODO insert installer code here in case searchGUI was not included????
        return true;
    }

    @Override
    public String getDescription() {
        return "Running SearchGUI";
    }
}
