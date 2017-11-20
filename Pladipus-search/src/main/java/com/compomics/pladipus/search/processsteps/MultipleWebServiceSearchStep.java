package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.search.util.IntegrationFromFile;
import com.compomics.pride_asa_pipeline.core.bypass.WebServiceMGFInference;
import com.compomics.pride_asa_pipeline.core.bypass.WebServiceParameterInference;
import com.compomics.util.experiment.biology.taxonomy.SpeciesFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.preferences.IdentificationParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class MultipleWebServiceSearchStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources;
    private final File fasta_repo;
    private static final Logger LOGGER = Logger.getLogger(MultipleWebServiceSearchStep.class);

    public MultipleWebServiceSearchStep() {
        tempResources = new File(System.getProperty("user.home") + "/pladipus/temp/search/resources");
        tempResources.getParentFile().mkdirs();
        fasta_repo = new File(System.getProperty("user.home") + "/pladipus/fasta");
        fasta_repo.mkdirs();
    }

    @Override
    public boolean doAction() throws PladipusProcessingException {
        //make sure nothing else clears the temp folder !
        parameters.put("skip_cleaning", "true");
        //initialize the temp folders
        prepareTempFolder();
        //prepare the output
        File outputFolder = new File(parameters.get("output_folder"));
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        //STEP 1. COLLECT THE SPECTRA
        String spectra = loadSpectra();
        //STEP 2. COLLECT THE PARAMETERS
        String parameterPath = loadParameters();
        //STEP 3. LAUNCH A SEARCH PER FASTA

        parameters.put("spectrum_files", spectra);
        parameters.put("id_params", parameterPath);
        String inputFastas = parameters.get("fasta_file");
        String[] fastaPaths = inputFastas.split(",");
        boolean initialized = false;
        for (String fastaPath : fastaPaths) {
            try {
                //set the output folder...
                File fastaFile = new File(fastaPath);
                File tmpOutputFolder = new File(outputFolder, fastaFile.getName().substring(0, 20));
                tmpOutputFolder.mkdirs();
                //Load the fasta into the parameters
                LoadFasta(fastaPath);
                //execute a search from file
                //make sure nothing gets deleted now...
                IntegrationFromFile.cleanTempDirectory = false;
                IntegrationFromFile.experimentFastaName = true;
                IntegrationFromFile.runNext(spectra, parameterPath, fastaPath, tmpOutputFolder, true);
            } catch (Exception ex) {
                throw new PladipusProcessingException(ex);
            }
        }
        return true;
    }

    private void prepareTempFolder() throws PladipusProcessingException {
        if (!parameters.containsKey("skip_cleaning") && tempResources.exists()) {
            LOGGER.info("Cleaning up resources : ");
            for (File aFile : tempResources.listFiles()) {
                if (aFile.exists()) {
                    LOGGER.info("Deleting " + aFile.getAbsoluteFile());
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

    }

    private String loadSpectra() {
        WebServiceMGFInference mgfInference = new WebServiceMGFInference(parameters.get("assay"));
        String spectrumPath = "";
        try {
            List<File> prideMGFFile = mgfInference.getPrideMGFFile(tempResources);
            spectrumPath = prideMGFFile.get(0).getAbsolutePath();
            parameters.put("spectrum_files", spectrumPath);
        } catch (Exception ex) {
            LOGGER.error("Could not retrieve MGF " + ex);
        }
        return spectrumPath;
    }

    private String loadParameters() {
        WebServiceParameterInference paramInference = new WebServiceParameterInference(parameters.get("assay"));
        String parameterPath = "";
        try {
            tempResources.mkdirs();
            File parameterFile = new File(tempResources, parameters.get("assay") + ".par");
            paramInference.InferParameters(parameterFile);
            parameterPath = parameterFile.getAbsolutePath();
        } catch (Exception ex) {
            LOGGER.error("Could not retrieve parameters " + ex);
            ex.printStackTrace();
        }
        return parameterPath;
    }

    private void LoadFasta(String fastaPath) throws Exception {
        String paramPath = parameters.get("id_params");
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
            if (!tempResources.exists()) {
                tempResources.mkdirs();
            }
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
