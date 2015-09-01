/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.blast;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
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
    private final File tempResources;

    public BlastSetupStep() {
        tempResources = new File(System.getProperty("user.home") + "/.compomics/temp");
    }

    @Override
    public boolean doAction() throws Exception {
        //cleanTempFolder();
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
        parameters.put("temp", tempResources.getAbsolutePath());
        //original
        String queryFasta = parameters.get("queryFasta");
        String dbFasta = parameters.get("dbFasta");

        //download these to the temp folder
        parameters.put("tempQueryFasta", PladipusFileDownloadingService.downloadFile(queryFasta, tempResources).getAbsolutePath());
        parameters.put("tempDbFasta", PladipusFileDownloadingService.downloadFile(dbFasta, tempResources).getAbsolutePath());
        parameters.put("blastFolder", getExecutable().getAbsolutePath());

    }

    public File getExecutable() throws IOException {
        //check if this is possible in another way...
        File toolFolder = new File(System.getProperties().getProperty("user.home") + "/.compomics/pladipus/tools/BLAST");
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        String blastFolderAddress = parameters.get("blastFolder");
        File BLASTFolder = PladipusFileDownloadingService.downloadFile(blastFolderAddress, toolFolder);

        if (BLASTFolder.getName().endsWith(".zip")) {
            File temp = new File(toolFolder, BLASTFolder.getName().replace(".zip", ""));
            ZipUtils.unzipArchive(BLASTFolder, temp);
            BLASTFolder = temp;
        }
        return BLASTFolder;
    }

    @Override
    public String getDescription() {
        return "Initialisation of the blasting process";
    }

}
