/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.msconvert;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class MsConvertSetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/MsConvert/resources");

    public MsConvertSetupStep() {

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
        String inputPath = parameters.get("f");

        parameters.put("f", PladipusFileDownloadingService.downloadFile(inputPath, tempResources).getAbsolutePath());

        //output
        File outputFolder = new File(parameters.get("o"));
        outputFolder.mkdirs();
    }

    @Override
    public String getDescription() {
        return "Initialisation of the MsConvert process";
    }

}
