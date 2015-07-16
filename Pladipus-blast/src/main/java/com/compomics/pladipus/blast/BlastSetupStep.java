/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.blast;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class BlastSetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources;

    public BlastSetupStep() {
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
        initialize();
        return true;
    }

    private void initialize() throws Exception {
        parameters.put("temp", tempResources.getAbsolutePath());
        //original
        String queryFasta = parameters.get("queryFasta");
        String dbFasta = parameters.get("dbFasta");
 
        //download these to the temp folder
        parameters.put("tempQueryFasta", PladipusFileDownloadingService.downloadFile(queryFasta, tempResources).getAbsolutePath());
        parameters.put("tempDbFasta", PladipusFileDownloadingService.downloadFile(dbFasta, tempResources).getAbsolutePath());
    }


    @Override
    public String getDescription() {
        return "Initialisation of the search process";
    }

}
