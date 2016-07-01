package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.preferences.IdentificationParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.httpclient.URIException;
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
    private final File fasta_repo;
    private static final Logger LOGGER = Logger.getLogger(SearchSetupStep.class);

    public SearchSetupStep() {
        tempResources = new File(System.getProperty("user.home") + "/pladipus/temp/search/resources");
        tempResources.getParentFile().mkdirs();
        fasta_repo = new File(System.getProperty("user.home") + "/pladipus/fasta");
        fasta_repo.mkdirs();
    }

    @Override
    public boolean doAction() throws PladipusProcessingException {
        if (!parameters.containsKey("skip_cleaning") && tempResources.exists()) {
            for (File aFile : tempResources.listFiles()) {
                if (aFile.exists()) {
                    if (aFile.isFile()) {
                        aFile.delete();
                    } else {
                        try {
                            FileUtils.deleteDirectory(aFile);
                        } catch (IOException ex) {
                            throw new PladipusProcessingException(ex);
                        }
                    }
                }
            }
        } else {
            tempResources.mkdirs();
        }
        try {
            initialiseInputFiles();
        } catch (Exception ex) {
            throw new PladipusProcessingException(ex);
        }
        return true;
    }

    private void initialiseInputFiles() throws Exception {
        //original
        String inputPath = parameters.get("spectrum_files");
        String paramPath = parameters.get("id_params");
        String fastaPath = parameters.get("fasta_file");

        //fix for older files that lack identification parameters
        if (!inputPath.equalsIgnoreCase(tempResources.getAbsolutePath())) {
            if (inputPath.toLowerCase().endsWith(".mgf") || inputPath.toLowerCase().endsWith(".mgf.zip")) {
                File downloadFile = PladipusFileDownloadingService.downloadFile(inputPath, tempResources);
                downloadFile.deleteOnExit();
                String inputFile = downloadFile.getAbsolutePath();

                if (inputPath.toLowerCase().endsWith(".zip")) {
                    LOGGER.debug("Unzipping input");
                    ZipUtils.unzipArchive(new File(inputFile), tempResources);
                    LOGGER.debug("Done unzipping...");
                }
                parameters.put("spectrum_files", inputFile.replace(".zip", ""));
            } else {
                parameters.put("spectrum_files", PladipusFileDownloadingService.downloadFolder(inputPath, tempResources).getAbsolutePath());
            }
        }
        LOGGER.debug("Got input files " + parameters.get("spectrum_files"));
        //generate a repo folder for fasta files...
        //clear the repository save for the current fasta (temporary solution)
        //TODO refactor that it deletes complete runs only
        String fastaName = new File(fastaPath).getName();
        boolean fastaAlreadyExists = false;
        File fastaFile = null;
        for (File aFasta : fasta_repo.listFiles()) {
            if (aFasta.getName().equalsIgnoreCase(fastaName)) {
                fastaFile = aFasta;
                fastaAlreadyExists = true;
                break;
            }
        }
        if (!fastaAlreadyExists) {
            fastaFile = PladipusFileDownloadingService.downloadFile(fastaPath, fasta_repo, fastaName);
        } else {
            for (File aFile : fasta_repo.listFiles()) {
                if (!aFile.getName().toLowerCase().contains(fastaName.toLowerCase())) {
                    aFile.delete();
                }
            }
        }

        if (fastaFile != null) {
            parameters.put("fasta_file", fastaFile.getAbsolutePath());
            LOGGER.debug("Got fasta file " + parameters.get("fasta_file"));
            //get and update parameters
            File paramFile;
            if (!paramPath.contains(tempResources.getAbsolutePath())) {
                paramFile = PladipusFileDownloadingService.downloadFile(paramPath, tempResources);
                parameters.put("id_params", paramFile.getAbsolutePath());
            }
            paramFile = new File(parameters.get("id_params"));
            SearchParameters sparameters = SearchParameters.getIdentificationParameters(paramFile);
            IdentificationParameters updatedIdentificationParameters = updateAlgorithmSettings(sparameters, fastaFile);
            IdentificationParameters.saveIdentificationParameters(updatedIdentificationParameters, paramFile);

            LOGGER.info("Got identification parameters " + parameters.get("id_params"));

            //output
            File outputFolder = new File(parameters.get("output_folder"));
            outputFolder.mkdirs();
            LOGGER.debug("Got output folder " + parameters.get("output_folder"));
            parameters.put("output_folder", outputFolder.getAbsolutePath());
        } else {
            throw new FileNotFoundException("Fasta file was not found !");
        }
    }

    @Override
    public String getDescription() {
        return "Initialisation of the search process";
    }

    public IdentificationParameters updateAlgorithmSettings(SearchParameters searchParameters, File fasta) throws URIException, IOException, XMLStreamException, URISyntaxException, UnspecifiedPladipusException {
        System.out.println("Updating the algorithm settings and setting the fasta file...");
        searchParameters.setFastaFile(fasta);
        SpeciesFactory.getInstance().initiate(new SearchGUIStep().getJar().getParentFile().getAbsolutePath());
        IdentificationParameters temp = new IdentificationParameters(searchParameters);
        return temp;
    }

    public static void main(String[] args) {
        ProcessingStep.main(args);
    }

}
