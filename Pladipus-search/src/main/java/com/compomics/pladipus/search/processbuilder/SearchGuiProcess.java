/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processbuilder;

import com.compomics.pladipus.core.control.runtime.diagnostics.memory.MemoryWarningSystem;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class SearchGuiProcess {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(SearchGuiProcess.class);
    /**
     * The SearchGUI jar location
     */
    private final File searchGUIJar;
    /**
     * The input mgf file
     */
    private final File mgf;
    /**
     * The input search parameters file
     */
    private final File searchParam;
    /**
     * The search output folder
     */
    private File outputFolder;
    /**
     * the user mod file from resources (should be reset between searches)
     */
    private File tempUserModFile;
    /**
     * the mod file from resources (should be reset between searches)
     */
    private File tempModFile;
    /**
     * The modification profile for this run (in case it needs
     * updating/correcting)
     */
    private PtmSettings ptmSettings;
    //todo refactor this into an enum
    /**
     * The available search engines for windows
     */
    private final String[] availablSearchEngineSwitches = new String[]{"xtandem", "msgf", "omssa", "ms_amanda", "myrimatch", "comet", "tide", "andromeda"};
    /**
     * The requested search engines
     */
    private final String[] requestedSearchEngines;

    public SearchGuiProcess(File mgf, File searchParam, File searchGUIJar, String[] requestedSearchEngines) {
        this.mgf = mgf;
        this.searchParam = searchParam;
        this.searchGUIJar = searchGUIJar;
        this.requestedSearchEngines = requestedSearchEngines;
    }

    private void parseSearchEngines(String[] requestedSearchEngines, List<String> command) {
        List<String> requestedSearchEngineList = Arrays.asList(requestedSearchEngines);
        String currentOS = System.getProperty("os.name").toLowerCase();
        System.out.println("Getting all potential engines for " + currentOS);
        if (currentOS.contains("mac") || currentOS.contains("sunos")) {
            throw new UnsupportedOperationException("Only linux and windows operating systems are currently supported");
        } else if (currentOS.contains("win")) {
            //all work on windows
        } else //andromeda doesn't work on LINUX
        if (requestedSearchEngineList.contains("andromeda")) {
            System.out.println("andromeda is not supported on " + currentOS);
            requestedSearchEngineList.remove("andromeda");
        }

        for (String anEngineSwitch : availablSearchEngineSwitches) {
            command.add("-" + anEngineSwitch);
            if (!requestedSearchEngineList.contains(anEngineSwitch.toLowerCase())) {
                command.add("0");
            } else {
                command.add("1");
            }
        }
    }

    /**
     *
     * @return the command line for the process
     * @throws NullPointerException
     */
    public List<String> generateCommand() throws NullPointerException {

        List<String> searchGUICommandLine = new ArrayList<>();
        searchGUICommandLine.add("java");
        searchGUICommandLine.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
        searchGUICommandLine.add("-cp");
        searchGUICommandLine.add(searchGUIJar.getAbsolutePath());
        searchGUICommandLine.add("eu.isas.searchgui.cmd.SearchCLI");
        searchGUICommandLine.add("-spectrum_files");
        searchGUICommandLine.add(mgf.getAbsolutePath());
        searchGUICommandLine.add("-output_folder");
        searchGUICommandLine.add(outputFolder.getAbsolutePath());
        searchGUICommandLine.add("-id_params");
        searchGUICommandLine.add(searchParam.getAbsolutePath());
        searchGUICommandLine.add("-output_option");
        searchGUICommandLine.add("0");
        searchGUICommandLine.add("-output_data");
        searchGUICommandLine.add("1");
        searchGUICommandLine.add("-temp_folder");
        searchGUICommandLine.add(System.getProperty("user.home") + "/.compomics/temp");
        parseSearchEngines(requestedSearchEngines, searchGUICommandLine);
        StringBuilder myCommand = new StringBuilder();
        for (String anArg : searchGUICommandLine) {
            myCommand.append(anArg).append(" ");
        }
        LOGGER.info("SEARCHGUI COMMAND LINE : " + System.lineSeparator() + myCommand.toString());
        //optionals
        return searchGUICommandLine;
    }

    public void setOutputFolder(File outputFolder) {
        if (outputFolder.exists()) {
            outputFolder.delete();
        }
        outputFolder.mkdir();
        this.outputFolder = outputFolder;
    }

    public File getSearchGuiJar() {
        return searchGUIJar;
    }

    PTMFactory factory = PTMFactory.getInstance();

    public void finalizeBuild() throws IOException, FileNotFoundException, ClassNotFoundException, Exception {
        finalizeFactory();
        tempUserModFile = new File(outputFolder, "temp_usermods.xml");
        if (tempUserModFile.exists()) {
            tempUserModFile.delete();
        }
        tempUserModFile.createNewFile();
        factory.saveFactory();
    }

    private void clearUserPTMs() throws IOException {
        List<String> toRemove = new ArrayList<>();
        for (String aPTMName : factory.getUserModifications()) {
            toRemove.add(aPTMName);
        }
        for (String aPTMName : toRemove) {
            factory.removeUserPtm(aPTMName);
        }
        factory.saveFactory();
    }

    private void finalizeFactory() throws Exception {
        SearchParameters identificationParameters = SearchParameters.getIdentificationParameters(searchParam);
        ptmSettings = identificationParameters.getPtmSettings();
        for (String aPtmName : ptmSettings.getAllModifications()) {
            PTM aPtm = ptmSettings.getPtm(aPtmName);
            if (!factory.containsPTM(aPtm.getName())) {
                //check for mass
                boolean add = true;
                for (String aPTMName : factory.getPTMs()) {
                    PTM factoryPtm = factory.getPTM(aPTMName);
                    if (factoryPtm.getMass() == aPtm.getMass()) {
                        LOGGER.info("There already is a PTM with this mass in the factory : " + factoryPtm.getName() + " - " + factoryPtm.getMass());
                        removeFromModProfile(ptmSettings, aPtm);
                        if (ptmSettings.getFixedModifications().contains(aPtmName)) {
                            ptmSettings.addFixedModification(factoryPtm);
                        } else {
                            ptmSettings.addVariableModification(factoryPtm);
                        }
                        add = false;
                        break;
                    }
                }
                if (add) {
                    factory.addUserPTM(aPtm);
                    LOGGER.info(aPtm.getName() + " was added");
                }
            } else {
                LOGGER.info("There already is a PTM with this name in the factory :" + aPtmName);
            }
        }
        identificationParameters.setPtmSettings(ptmSettings);
        SearchParameters.saveIdentificationParameters(identificationParameters, searchParam);
    }

    private void removeFromModProfile(PtmSettings ptmSettings, PTM aPtm) {
        if (ptmSettings.getFixedModifications().contains(aPtm.getName())) {
            ptmSettings.removeFixedModification(aPtm.getName());
        } else {
            ptmSettings.removeVariableModification(aPtm.getName());
        }
    }

}
