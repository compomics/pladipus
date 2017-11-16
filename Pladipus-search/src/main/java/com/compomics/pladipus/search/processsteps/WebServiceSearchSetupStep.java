package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
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
import javax.xml.stream.XMLStreamException;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class WebServiceSearchSetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final File tempResources;
    private final File fasta_repo;
    private static final Logger LOGGER = Logger.getLogger(WebServiceSearchSetupStep.class);

    public WebServiceSearchSetupStep() {
        tempResources = new File(System.getProperty("user.home") + "/pladipus/temp/search/resources");
        tempResources.getParentFile().mkdirs();
        fasta_repo = new File(System.getProperty("user.home") + "/pladipus/fasta");
        fasta_repo.mkdirs();
    }

    @Override
    public boolean doAction() throws PladipusProcessingException {
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
        try {
            initialiseInputFiles();
        } catch (Exception ex) {
            throw new PladipusProcessingException(ex);
        }
        return true;
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
            File parameterFile = new File(tempResources, parameters.get("assay") + ".par");
            paramInference.InferParameters(parameterFile);
            parameterPath = parameterFile.getAbsolutePath();

        } catch (Exception ex) {
            LOGGER.error("Could not retrieve parameters " + ex);
        }
        return parameterPath;
    }

    private void initialiseInputFiles() throws Exception {
        //original
        parameters.put("spectrum_files", loadSpectra());
        parameters.put("id_params", loadParameters());
        String fastaPath = parameters.get("fasta_file");
        LoadFasta(fastaPath);
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
