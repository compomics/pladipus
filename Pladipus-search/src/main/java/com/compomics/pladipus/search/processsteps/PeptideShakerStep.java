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
import com.compomics.pladipus.core.model.enums.AllowedPeptideShakerParams;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.search.checkpoints.PeptideShakerCheckPoints;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerStep extends ProcessingStep {

    private final File temp_output_folder = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/PeptideShaker/result");
    private File temp_output_file;
    private File real_output_folder;
    private File real_output_file;

    public PeptideShakerStep() {

    }

    private List<String> constructArguments() throws IOException {
        File peptideShakerJar = getJar();
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideShakerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.PeptideShakerCLI");
        for (AllowedPeptideShakerParams aParameter : AllowedPeptideShakerParams.values()) {
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
        System.out.println("Running " + this.getClass().getName());
        File peptideShakerJar = getJar();

        if (temp_output_folder.exists()) {
            temp_output_folder.delete();
        }
        temp_output_folder.mkdirs();

        String experiment = "output";

        if (parameters.containsKey("experiment")) {
            experiment = parameters.get("experiment");
        }

        real_output_folder = new File(parameters.get("out")).getParentFile();
        if (parameters.containsKey("output_folder")) {
            temp_output_file = new File(temp_output_folder + "/" + experiment + ".cps");
            parameters.put("out", temp_output_file.getAbsolutePath());
            parameters.remove("output_folder");
        }
        real_output_file=new File(real_output_folder,temp_output_file.getName());
      

        List<String> constructArguments = constructArguments();
        //add callback notifier for more detailed printouts of the processing
        CallbackNotifier callbackNotifier = getCallbackNotifier();
        for (PeptideShakerCheckPoints aCheckPoint : PeptideShakerCheckPoints.values()) {
            callbackNotifier.addCheckpoint(new Checkpoint(aCheckPoint.getLine(), aCheckPoint.getFeedback()));
        }
        new ProcessingEngine().startProcess(peptideShakerJar, constructArguments, callbackNotifier);
        //run peptideShaker with the existing files
        cleanupAndSave();
        return true;
    }

    public File getJar() throws IOException {
        //check if this is possible in another way...
        File toolFolder = new File(System.getProperties().getProperty("user.home") + "/.compomics/pladipus/tools");
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        File temp = new File(toolFolder, "PeptideShaker");
        if (!temp.exists()) {
            File peptideshakerFile = PladipusFileDownloadingService.downloadFile(parameters.get("PeptideShaker"), toolFolder);
            if (peptideshakerFile.getName().endsWith(".zip")) {
                ZipUtils.unzipArchive(peptideshakerFile, temp);
            }
        }
        return JarLookupService.lookupFile("PeptideShaker-.*.jar", temp);
    }

    public boolean aVersionExistsLocal() {
        //TODO insert installer code here in case PeptideShaker was not included????
        return true;
    }

    private void cleanupAndSave() throws IOException {
        System.out.println("Running " + this.getClass().getName());
        //parameters.put("out",real_output_file.getAbsolutePath());
        real_output_file.getParentFile().mkdirs();
        //copy as a stream?
        if (!real_output_file.exists()) {
            real_output_file.createNewFile();
        }
        try (FileChannel source = new FileInputStream(temp_output_file).getChannel();
                FileChannel destination = new FileOutputStream(real_output_file).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
        //check if reports should be made
        if (!parameters.containsKey("generate_reports")) {
            FileUtils.deleteDirectory(temp_output_folder);
        }else{
            parameters.put("out_reports",real_output_file.getParentFile().getAbsolutePath());
        }
        
    }

    @Override
    public String getDescription() {
        return "Running PeptideShaker";
    }
}
