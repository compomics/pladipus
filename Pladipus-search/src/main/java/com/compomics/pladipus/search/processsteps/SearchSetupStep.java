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
        tempResources = new File(System.getProperty("user.home") + "/.compomics/temp");
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
        String inputPath = parameters.get("input");
        String paramPath = parameters.get("parameterFile");
        String fastaPath = parameters.get("fastaFile");

        if (inputPath.toLowerCase().endsWith(".mgf")) {
            parameters.put("tempInput", PladipusFileDownloadingService.downloadFile(inputPath, tempResources).getAbsolutePath());
        } else {
            parameters.put("tempInput", PladipusFileDownloadingService.downloadFolder(inputPath, tempResources).getAbsolutePath());
        }

        parameters.put("tempParameterFile", PladipusFileDownloadingService.downloadFile(paramPath, tempResources).getAbsolutePath());

        parameters.put("tempFastaFile", PladipusFileDownloadingService.downloadFile(fastaPath, tempResources, "temp.fasta").getAbsolutePath());

        //output
        File outputFolder = new File(parameters.get("outputFolder") + "/" + parameters.get("assay"));
        outputFolder.mkdirs();
        File tempFolder =new File(System.getProperty("user.home")+"/.compomics/pladipus/temp/SearchGUI");
        tempFolder.mkdirs();
        parameters.put("outputfolder", outputFolder.getAbsolutePath());
        parameters.put("temp", tempFolder.getAbsolutePath());
        
        System.out.println(parameters);
    }

    @Override
    public String getDescription() {
        return "Initialisation of the search process";
    }

}
