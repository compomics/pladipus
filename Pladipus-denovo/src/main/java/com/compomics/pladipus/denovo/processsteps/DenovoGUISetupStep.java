/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.denovo.processsteps;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class DenovoGUISetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources;

    public DenovoGUISetupStep() {
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
        InitialiseInputFiles();
        return true;
    }

    private void InitialiseInputFiles() throws Exception {
        //original
        String mgfPath = parameters.get("input");

        String paramPath = parameters.get("parameterFile");

        File outputFolder = new File(parameters.get("outputFolder"));
        outputFolder.mkdirs();
        parameters.put("outputFolder", outputFolder.getAbsolutePath());

        if (mgfPath.toLowerCase().endsWith(".mgf")) {
            parameters.put("tempInput", PladipusFileDownloadingService.downloadFile(mgfPath, tempResources).getAbsolutePath());
        } else {
            parameters.put("tempInput", PladipusFileDownloadingService.downloadFolder(mgfPath, tempResources).getAbsolutePath());
        }
        parameters.put("tempParameterFile", PladipusFileDownloadingService.downloadFile(paramPath, tempResources).getAbsolutePath());

        //output
        parameters.put("temp", tempResources.getAbsolutePath());
        //load the tools here?
        System.out.println(parameters);
    }

    @Override
    public String getDescription() {
        return "Initialisation of the denovoGUI process";
    }

}
