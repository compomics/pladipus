/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.denovo.processsteps;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.util.JarLookupService;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.denovo.processbuilder.DenovoGUIProcess;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Kenneth Verheggen
 */
public class DenovoGUIStep extends ProcessingStep {

    public DenovoGUIStep() {

    }

    @Override
    public boolean doAction() throws Exception, Exception {
            //check if searchgui is local, if not download it
        
        if (!aVersionExistsLocal()) {
            //  downloadSearchGUI();
        }
        File peakFile = new File(parameters.get("tempInput"));
        File parameterFile = new File(parameters.get("tempParameterFile"));
        File outputFolder = new File(parameters.get("outputFolder"));

        DenovoGUIProcess process = new DenovoGUIProcess(outputFolder, peakFile, parameterFile, getJar());

        File temp = new File(parameters.get("temp"));
        process.setOutputFolder(temp);
        ProcessingEngine.startProcess(getJar(), process.generateCommand());
        return true;
    }

   public File getJar() throws IOException {
        //check if this is possible in another way...
        File toolFolder = new File(System.getProperties().getProperty("user.home") + "/.compomics/pladipus/tools");
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        File temp = new File(toolFolder, "DeNovoGUI");
        if (!temp.exists()) {
            File searchGUIFile = PladipusFileDownloadingService.downloadFile(parameters.get("DeNovoGUI"), toolFolder);
            if (searchGUIFile.getName().endsWith(".zip")) {
                ZipUtils.unzipArchive(searchGUIFile, temp);
            }
        }
        return JarLookupService.lookupFile("DeNovoGUI-.*.jar", temp);
    }

    public boolean aVersionExistsLocal() {
        //TODO insert installer code here in case searchGUI was not included????
        return true;
    }

    @Override
    public String getDescription() {
        return "Running DenovoGUI";
    }

}
