/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class SearchSetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources;

    public SearchSetupStep() {
        tempResources = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/SearchGUI/resources");
    }

    @Override
    public boolean doAction() throws Exception {
        System.out.println("Running " + this.getClass().getName());
        if (tempResources.exists()) {
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
        initialiseInputFiles();
        return true;
    }

    private void initialiseInputFiles() throws Exception {
        //original
        String inputPath = parameters.get("spectrum_files");
        String paramPath = parameters.get("id_params");
        String fastaPath = parameters.get("fasta_file");

        if (inputPath.toLowerCase().endsWith(".mgf")) {
            parameters.put("spectrum_files", PladipusFileDownloadingService.downloadFile(inputPath, tempResources).getAbsolutePath());
        } else {
            parameters.put("spectrum_files", PladipusFileDownloadingService.downloadFolder(inputPath, tempResources).getAbsolutePath());
        }

        parameters.put("id_params", PladipusFileDownloadingService.downloadFile(paramPath, tempResources).getAbsolutePath());

        parameters.put("fasta_file", PladipusFileDownloadingService.downloadFile(fastaPath, tempResources, "temp.fasta").getAbsolutePath());

        //output
        File outputFolder = new File(parameters.get("output_folder") + "/" + parameters.get("title"));
        outputFolder.mkdirs();
        parameters.put("output_folder", outputFolder.getAbsolutePath());
    }

    @Override
    public String getDescription() {
        return "Initialisation of the search process";
    }

}
