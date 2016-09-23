package com.compomics.pladipus.msconvert;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class MsConvertSetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final Path tempResources = Paths.get(System.getProperty("user.home") + "/pladipus/temp/MsConvert/resources");

    public MsConvertSetupStep() {

    }

    @Override
    public boolean doAction() throws PladipusProcessingException {
        System.out.println("Running " + this.getClass().getName());
        try {
            Files.createTempDirectory(tempResources,"msconvert");
            initialiseInputFiles();
        } catch (Exception ex) {
            throw new PladipusProcessingException(ex);
        }
        return true;
    }

    private void initialiseInputFiles() throws Exception {
        //original
        String inputPath = parameters.get("f");
        PladipusFileDownloadingService.downloadFile(inputPath, tempResources.toFile());
        parameters.put("f", tempResources.resolve(new File(inputPath).getName()).toString());


        //output
        File outputFolder = new File(parameters.get("o"));
        outputFolder.mkdirs();
    }

    @Override
    public String getDescription() {
        return "Initialisation of the MsConvert process";
    }

        public static void main(String[] args) {
        ProcessingStep.main(args);
    }
    
}
