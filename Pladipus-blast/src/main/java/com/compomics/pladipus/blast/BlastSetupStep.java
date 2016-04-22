package com.compomics.pladipus.blast;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class BlastSetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources = new File(System.getProperty("user.home") + "/pladipus/temp/blast");

    public BlastSetupStep() {

    }

    @Override
    public boolean doAction() throws UnspecifiedPladipusException {
        try {
            cleanTempFolder();
            initialize();
            return true;
        } catch (Exception ex) {
            throw new UnspecifiedPladipusException(ex);
        }
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
