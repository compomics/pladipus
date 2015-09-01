/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.search.processbuilder.PeptideShakerProcess;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerStep extends ProcessingStep {

    public PeptideShakerStep() {

    }

    @Override
    public boolean doAction() throws Exception, Exception {
        System.out.println("Running " + this.getClass().getName());
        File peptideShakerJar = getJar();
        
        File parameterFile = new File(parameters.get("tempParameterFile"));
        File input = new File(parameters.get("input"));
        
        
        File temp = new File(parameters.get("temp"));
      
        PeptideShakerProcess process = new PeptideShakerProcess(parameters.get("assay"), input, parameterFile, peptideShakerJar);
        parameters.put("cps", process.getResultingCpsFile().getAbsolutePath());
        //TODO REPLACE THIS WITH THE ACTUAL OUTPUTFOLDER OR WAIT TILL THE VERY END IN THE CLEANING STEP?
        process.setOutputFolder(temp);
        ProcessingEngine.startProcess(peptideShakerJar, process.generateCommand());
        //run peptideShaker with the existing files
        cleanupAndSave();
        return true;
    }

    public File getJar() throws IOException {
        //check if this is possible in another way...
        File toolFolder = new File(System.getProperties().getProperty("user.home") + "/.compomics/pladipus/tools");
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        File temp = new File(toolFolder,"PeptideShaker");
        File denovoGUIFile = PladipusFileDownloadingService.downloadFile(parameters.get("PeptideShaker"), toolFolder);
        if (denovoGUIFile.getName().endsWith(".zip")) {
            ZipUtils.unzipArchive(denovoGUIFile, temp);
        }
        File jarParent = temp.listFiles()[0];
        String version = jarParent.getName();
   //     version=version.substring(0,version.indexOf("-"));
        return new File(jarParent, version + ".jar");
    }

    public boolean aVersionExistsLocal() {
        //TODO insert installer code here in case searchGUI was not included????
        return true;
    }

    private void cleanupAndSave() throws IOException {

        System.out.println("Running " + this.getClass().getName());
        //clean all the file-extensions that are not in the list to be saved
        File temp = new File(parameters.get("temp"));

        File[] files = temp.listFiles(new FileFilter() {
            private final FileNameExtensionFilter filter
                    = new FileNameExtensionFilter("Result Files", "cps");

            @Override
            public boolean accept(File file) {
                return (!file.isDirectory() && filter.accept(file));
            }
        });

        File outputFolder = new File(parameters.get("outputFolder"));
        outputFolder.mkdirs();
        for (File aFile : files) {
            File dest = new File(outputFolder, aFile.getName());
            if (aFile.isDirectory()) {
                FileUtils.copyDirectory(aFile, dest, true);
            } else {
                FileUtils.copyFile(aFile, dest, true);
            }
        }
        FileUtils.deleteDirectory(temp);
    }

    @Override
    public String getDescription() {
        return "Running PeptideShaker";
    }
}
