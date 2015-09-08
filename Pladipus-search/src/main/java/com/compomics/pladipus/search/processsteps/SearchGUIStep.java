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
import com.compomics.pladipus.search.processbuilder.SearchGuiProcess;
import com.compomics.pladipus.search.util.JarLookupService;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Kenneth Verheggen
 */
public class SearchGUIStep extends ProcessingStep {

    public SearchGUIStep() {

    }

    @Override
    public boolean doAction() throws Exception, Exception {
        System.out.println("Running " + this.getClass().getName());
        //check if searchgui is local, if not download it
        if (!aVersionExistsLocal()) {
            //  downloadSearchGUI();
        }
        File input = new File(parameters.get("tempInput"));
        File parameterFile = new File(parameters.get("tempParameterFile"));
        File fastaFile = new File(parameters.get("tempFastaFile"));
        //update the fasta
        System.out.println("Getting parameterFile : " + parameterFile);
        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(parameterFile);
        identificationParameters.setFastaFile(fastaFile);
        //update other parameters or keep defaults for now?
        //update mods ---> fixed / var
        PtmSettings ptmSettings = identificationParameters.getPtmSettings();
        ArrayList<String> tempMods = new ArrayList<>();
        tempMods.addAll(ptmSettings.getFixedModifications());
        for (String aPTM : tempMods) {
            //special case...itraq on y
            if (aPTM.toLowerCase().contains("itraq114 on y")) {
                ptmSettings.addVariableModification(ptmSettings.getPtm(aPTM));
                ptmSettings.removeFixedModification(aPTM);
            }
            //all other fixed...
            if (!aPTM.toLowerCase().contains("itraq")
                    && !aPTM.toLowerCase().contains("tmt")
                    && !aPTM.toLowerCase().contains("carbamidomethyl")) {
                ptmSettings.addVariableModification(ptmSettings.getPtm(aPTM));
                ptmSettings.removeFixedModification(aPTM);
            }
        }
        //
        identificationParameters.setPtmSettings(ptmSettings);
        System.out.println("Setting searchparameter settings location [FIX]");
        identificationParameters.setParametersFile(parameterFile);
        SearchParameters.saveIdentificationParameters(identificationParameters, parameterFile);

        //get requested search engines
        String searchEngines = parameters.get("searchEngines").toLowerCase();
        if (searchEngines == null || searchEngines.isEmpty()) {
            System.out.println("No search engines requested, defaulting to xTandem, Tide and MSGF+");
            searchEngines = "xtandem,tide,msgf";
        }
        System.out.println("Selected engines : " + searchEngines);
        SearchGuiProcess process = new SearchGuiProcess(input, parameterFile, getJar(), searchEngines.split(","));
        File temp = new File(parameters.get("temp"));
        process.setOutputFolder(temp);
       // process.finalizeBuild();
        System.out.println("Starting process !");
        ProcessingEngine.startProcess(getJar(), process.generateCommand());
        parameters.put("input", temp.getAbsolutePath());
        return true;
    }

    public File getJar() throws IOException {
        //check if this is possible in another way...
        File toolFolder = new File(System.getProperties().getProperty("user.home") + "/.compomics/pladipus/tools");
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        File temp = new File(toolFolder, "SearchGUI");
        if (!temp.exists()) {
            File searchGUIFile = PladipusFileDownloadingService.downloadFile(parameters.get("SearchGUI"), toolFolder);
            if (searchGUIFile.getName().endsWith(".zip")) {
                ZipUtils.unzipArchive(searchGUIFile, temp);
            }
        }
        return JarLookupService.lookupFile("SearchGUI-.*.jar", temp);
    }

    public boolean aVersionExistsLocal() {
        //TODO insert installer code here in case searchGUI was not included????
        return true;
    }

    @Override
    public String getDescription() {
        return "Running SearchGUI";
    }

}
