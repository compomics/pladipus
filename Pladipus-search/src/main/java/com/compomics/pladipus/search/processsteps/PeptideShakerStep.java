/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.util.JarLookupService;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.enums.AllowedPeptideShakerParams;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerStep extends ProcessingStep {

    private final File temp_output_folder = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/PeptideShaker/result");

    public PeptideShakerStep() {

    }

    private List<String> constructArguments() throws IOException {
        File peptideShakerJar = getJar();
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
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

        if (parameters.containsKey("output_folder")) {
            parameters.put("out", new File(parameters.get("output_folder") + "/" + experiment + ".cps").getAbsolutePath());
            parameters.remove("output_folder");
        }

        File real_output_folder = new File(parameters.get("out")).getParentFile();

        List<String> constructArguments = constructArguments();
        //TODO REPLACE THIS WITH THE ACTUAL OUTPUTFOLDER OR WAIT TILL THE VERY END IN THE CLEANING STEP?
        ProcessingEngine.startProcess(peptideShakerJar, constructArguments);
        //run peptideShaker with the existing files
        cleanupAndSave(real_output_folder);
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

    private void cleanupAndSave(File realTargetFolder) throws IOException {

        System.out.println("Running " + this.getClass().getName());

        File outputFolder = new File(parameters.get("out")).getParentFile();
        outputFolder.mkdirs();
        for (File aFile : temp_output_folder.listFiles()) {
            File dest = new File(realTargetFolder, aFile.getName());
            if (aFile.isDirectory()) {
                FileUtils.copyDirectory(aFile, dest, true);
            } else {
                FileUtils.copyFile(aFile, dest, true);
            }
        }
        FileUtils.deleteDirectory(temp_output_folder);
    }

    @Override
    public String getDescription() {
        return "Running PeptideShaker";
    }
}
