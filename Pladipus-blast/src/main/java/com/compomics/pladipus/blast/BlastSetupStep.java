/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.blast;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class BlastSetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/blast");

    public BlastSetupStep() {
        
    }

    @Override
    public boolean doAction() throws Exception {
        cleanTempFolder();
        initialize();
        return true;
    }

    private void cleanTempFolder() throws IOException {
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
    }

    private void initialize() throws Exception {
        //original
        parameters.put("query", PladipusFileDownloadingService.downloadFile(parameters.get("query"), tempResources).getAbsolutePath());
        parameters.put("db", PladipusFileDownloadingService.downloadFile(parameters.get("db"), tempResources).getAbsolutePath());
    }


    @Override
    public String getDescription() {
        return "Initialisation of the blasting process";
    }

}
