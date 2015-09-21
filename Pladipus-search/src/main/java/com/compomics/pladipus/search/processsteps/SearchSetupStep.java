package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class SearchSetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources;
    private static final Logger LOGGER = Logger.getLogger(SearchSetupStep.class);

    public SearchSetupStep() {
        tempResources = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/SearchGUI/resources");
        tempResources.getParentFile().mkdirs();
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
        String inputPath = parameters.get("spectrum_files");
        String paramPath = parameters.get("id_params");
        String fastaPath = parameters.get("fasta_file");

        if (inputPath.toLowerCase().endsWith(".mgf") || inputPath.toLowerCase().endsWith(".mgf.zip")) {
            String inputFile = PladipusFileDownloadingService.downloadFile(inputPath, tempResources).getAbsolutePath();
            if (inputPath.toLowerCase().endsWith(".zip")) {
                LOGGER.info("Unzipping input");
                ZipUtils.unzipArchive(new File(inputFile), tempResources);
                LOGGER.info("Done unzipping...");
            }
            parameters.put("spectrum_files", inputFile.replace(".zip", ""));
        } else {
            parameters.put("spectrum_files", PladipusFileDownloadingService.downloadFolder(inputPath, tempResources).getAbsolutePath());
        }
        LOGGER.info("Got input files " + parameters.get("spectrum_files"));
        parameters.put("id_params", PladipusFileDownloadingService.downloadFile(paramPath, tempResources).getAbsolutePath());
        LOGGER.info("Got identification parameters " + parameters.get("id_params"));
        parameters.put("fasta_file", PladipusFileDownloadingService.downloadFile(fastaPath, tempResources, "temp.fasta").getAbsolutePath());
        LOGGER.info("Got fasta file " + parameters.get("fasta_file"));
        //output
        File outputFolder = new File(parameters.get("output_folder"));
        outputFolder.mkdirs();
        LOGGER.info("Got output folder " + parameters.get("output_folder"));
        parameters.put("output_folder", outputFolder.getAbsolutePath());
    }

    @Override
    public String getDescription() {
        return "Initialisation of the search process";
    }
}
