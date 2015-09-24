package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.AndromedaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.CometParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.DirecTagParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsAmandaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MsgfParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.MyriMatchParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.OmssaParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.PNovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.PepnovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.TideParameters;
import com.compomics.util.experiment.identification.identification_parameters.tool_specific.XtandemParameters;
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

        //fix for older files that lack identification parameters
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
        File fastaFile = PladipusFileDownloadingService.downloadFile(fastaPath, tempResources, "temp.fasta");
        parameters.put("fasta_file", fastaFile.getAbsolutePath());
        LOGGER.info("Got fasta file " + parameters.get("fasta_file"));
        //get and update parameters
        File paramFile = PladipusFileDownloadingService.downloadFile(paramPath, tempResources);
        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(paramFile);
        SearchParameters updatedIdentificationParameters = updateAlgorithmSettings(identificationParameters, fastaFile);
        SearchParameters.saveIdentificationParameters(updatedIdentificationParameters, paramFile);
        parameters.put("id_params", paramFile.getAbsolutePath());
        LOGGER.info("Got identification parameters " + parameters.get("id_params"));

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

    public SearchParameters updateAlgorithmSettings(SearchParameters searchParameters, File fasta) {
        SearchParameters tempSearchParameters = new SearchParameters();
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.omssa.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.omssa.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.omssa.getIndex(), new OmssaParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.xtandem.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.xtandem.getIndex(), new XtandemParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.msgf.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.msgf.getIndex(), new MsgfParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.msAmanda.getIndex(), new MsAmandaParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.myriMatch.getIndex(), new MyriMatchParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.comet.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.comet.getIndex(), new CometParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.tide.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.tide.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.tide.getIndex(), new TideParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.andromeda.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.andromeda.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.andromeda.getIndex(), new AndromedaParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.pepnovo.getIndex(), new PepnovoParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.direcTag.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.direcTag.getIndex(), new DirecTagParameters());
        }
        if (searchParameters.getIdentificationAlgorithmParameter(Advocate.pNovo.getIndex()) != null) {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.pNovo.getIndex(), searchParameters.getIdentificationAlgorithmParameter(Advocate.pNovo.getIndex()));
        } else {
            tempSearchParameters.setIdentificationAlgorithmParameter(Advocate.pNovo.getIndex(), new PNovoParameters());
        }
        //set the fasta file
        tempSearchParameters.setFastaFile(fasta);
        //
        tempSearchParameters.setEnzyme(searchParameters.getEnzyme());
        tempSearchParameters.setPrecursorAccuracy(searchParameters.getPrecursorAccuracy());
        tempSearchParameters.setFragmentIonAccuracy(searchParameters.getFragmentIonAccuracy());
        tempSearchParameters.setPrecursorAccuracyType(searchParameters.getPrecursorAccuracyType());
        tempSearchParameters.setFragmentAccuracyType(searchParameters.getFragmentAccuracyType());
        tempSearchParameters.setPtmSettings(searchParameters.getPtmSettings());
        tempSearchParameters.setnMissedCleavages(searchParameters.getnMissedCleavages());
        tempSearchParameters.setMaxChargeSearched(searchParameters.getMaxChargeSearched());
        tempSearchParameters.setMinChargeSearched(searchParameters.getMinChargeSearched());
        System.out.println(tempSearchParameters.toString());
        
     
        
        return tempSearchParameters;
    }
}
