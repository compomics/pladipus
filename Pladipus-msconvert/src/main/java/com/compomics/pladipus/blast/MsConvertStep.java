/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.blast;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class MsConvertStep extends ProcessingStep {

    public MsConvertStep() {

    }

    @Override
    public boolean doAction() throws Exception {
        System.out.println("Running " + this.getClass().getName());
        if (checkOS()) {
            //check if searchgui is local, if not download it
            File input = new File(parameters.get("raw_input"));
            File parameterFile = new File(parameters.get("convert_param"));
            File output = new File(parameters.get("mgf_output"));
            File executable = new File(parameters.get("pwiz_folder"), "msconvert");

            //download the input to a temp folder?
            File tempResources = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/msconvert");
            tempResources.mkdirs();
            File tempRAW = new File(PladipusFileDownloadingService.downloadFile(input.getAbsolutePath(), tempResources).getAbsolutePath());
            File tempMGF = new File(tempRAW.getParentFile(), output.getName());

            //convert the RAW file
            MsConvertProcess process = new MsConvertProcess(tempRAW, tempMGF, parameterFile, executable);
            ProcessingEngine.startProcess(executable, process.generateCommand());

            //deliver the file to the correct location
            FileUtils.copyFile(tempMGF, output, true);
            //delete the tempfolder
            tempRAW.delete();
            tempMGF.delete();
            FileUtils.deleteDirectory(tempResources);

            return true;
        }
        return false;
    }

    public boolean aVersionExistsLocal() {
        //TODO insert installer code here in case PWIZ was not installed???
        return true;
    }

    @Override
    public String getDescription() {
        return "Running SearchGUI";
    }

    private boolean checkOS() {
        String currentOS = System.getProperty("os.name").toLowerCase();
        if (currentOS.contains("win")) {
            return true;
        }
        throw new UnsupportedOperationException("MsConvert uses vendor specific libraries only available on windows !");
    }

}
