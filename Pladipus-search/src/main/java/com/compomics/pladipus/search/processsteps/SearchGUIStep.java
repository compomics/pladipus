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
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.preferences.ModificationProfile;
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
        ModificationProfile modificationProfile = identificationParameters.getModificationProfile();
        ArrayList<String> tempMods = new ArrayList<>();
        tempMods.addAll(modificationProfile.getFixedModifications());
        for (String aPTM : tempMods) {
            //special case...itraq on y
            if (aPTM.toLowerCase().contains("itraq114 on y")) {
                modificationProfile.addVariableModification(modificationProfile.getPtm(aPTM));
                modificationProfile.removeFixedModification(aPTM);
            }
            //all other fixed...
            if (!aPTM.toLowerCase().contains("itraq")
                    && !aPTM.toLowerCase().contains("tmt")
                    && !aPTM.toLowerCase().contains("carbamidomethyl")) {
                modificationProfile.addVariableModification(modificationProfile.getPtm(aPTM));
                modificationProfile.removeFixedModification(aPTM);
            }
        }
        //
        identificationParameters.setModificationProfile(modificationProfile);
        SearchParameters.saveIdentificationParameters(identificationParameters, parameterFile);

        //get requested search engines
        String searchEngines = parameters.get("searchEngines");
        if (searchEngines == null) {
            searchEngines = "xtandem,tide";
        }
        SearchGuiProcess process = new SearchGuiProcess(input, parameterFile, getJar(), searchEngines.split(","));
        File temp = new File(parameters.get("temp"));
        process.setOutputFolder(temp);
        process.finalizeBuild();
        ProcessingEngine.startProcess(getJar(), process.generateCommand());
        parameters.put("input",temp.getAbsolutePath());
        return true;
    }

    
    public File getJar() throws IOException {
        //check if this is possible in another way...
        File toolFolder = new File(System.getProperties().getProperty("user.home") + "/.compomics/pladipus/tools");
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        File temp = new File(toolFolder,"SearchGUI");
        File denovoGUIFile = PladipusFileDownloadingService.downloadFile(parameters.get("SearchGUI"), toolFolder);
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

    @Override
    public String getDescription() {
        return "Running SearchGUI";
    }

}
