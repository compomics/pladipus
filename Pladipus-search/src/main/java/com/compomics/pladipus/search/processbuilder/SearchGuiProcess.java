/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processbuilder;

import com.compomics.pladipus.core.control.runtime.diagnostics.memory.MemoryWarningSystem;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.preferences.ModificationProfile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.xmlpull.v1.XmlPullParserException;

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
    private ModificationProfile modificationProfile;
    /**
     * The available search engines CAUTION be careful in case some engines
     * don't run on a particular OS
     */
    private final String[] availableSearchEngineSwitches = new String[]{"xtandem", "msgf", "ms_amanda", "myrimatch", "comet", "tide"};
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
        for (String anEngineSwitch : availableSearchEngineSwitches) {
            if (!requestedSearchEngineList.contains(anEngineSwitch)) {
                command.add("-" + anEngineSwitch);
                command.add("0");
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
        searchGUICommandLine.add("3");
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
        outputFolder.mkdirs();
        this.outputFolder = outputFolder;
    }

    public File getSearchGuiJar() {
        return searchGUIJar;
    }

    PTMFactory factory = PTMFactory.getInstance();

    public void finalizeBuild() throws IOException, FileNotFoundException, ClassNotFoundException, Exception {
        importDefaultMods();
        finalizeFactory();
        tempUserModFile = new File(outputFolder, "temp_usermods.xml");
        if (tempUserModFile.exists()) {
            tempUserModFile.delete();
        }
        tempUserModFile.createNewFile();
        factory.setSearchedOMSSAIndexes(modificationProfile);
        factory.writeOmssaUserModificationFile(tempUserModFile);
        factory.saveFactory();
    }

    private void importDefaultMods() throws IOException, XmlPullParserException {
        tempModFile = new File(outputFolder, "temp_mods.xml");
        InputStream inputStream = new ClassPathResource("searchGUI_mods.xml").getInputStream();
        OutputStream outputStream = new FileOutputStream(tempModFile);
        IOUtils.copy(inputStream, outputStream);
        tempModFile.deleteOnExit();
        factory.clearFactory();
        factory = PTMFactory.getInstance();
        factory.importModifications(tempModFile, false, true);
        clearUserPTMs();
        tempUserModFile = new File(outputFolder, "temp_usermods.xml");
        tempUserModFile.deleteOnExit();
        inputStream = new ClassPathResource("searchGUI_usermods.xml").getInputStream();
        outputStream = new FileOutputStream(tempUserModFile);
        IOUtils.copy(inputStream, outputStream);
        factory.importModifications(tempUserModFile, true, true);
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
        modificationProfile = identificationParameters.getModificationProfile();
        for (String aPtmName : modificationProfile.getAllModifications()) {
            PTM aPtm = modificationProfile.getPtm(aPtmName);
            if (!factory.containsPTM(aPtm.getName())) {
                //check for mass
                boolean add = true;
                for (String aPTMName : factory.getPTMs()) {
                    PTM factoryPtm = factory.getPTM(aPTMName);
                    if (factoryPtm.getMass() == aPtm.getMass()) {
                        LOGGER.info("There already is a PTM with this mass in the factory : " + factoryPtm.getName() + " - " + factoryPtm.getMass());
                        removeFromModProfile(modificationProfile, aPtm);
                        if (modificationProfile.getFixedModifications().contains(aPtmName)) {
                            modificationProfile.addFixedModification(factoryPtm);
                        } else {
                            modificationProfile.addVariableModification(factoryPtm);
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
        factory.setSearchedOMSSAIndexes(modificationProfile);
        factory.writeOmssaUserModificationFile(tempUserModFile);
        factory.importModifications(tempUserModFile, true);
        identificationParameters.setModificationProfile(modificationProfile);
        SearchParameters.saveIdentificationParameters(identificationParameters, searchParam);
    }

    private void removeFromModProfile(ModificationProfile profile, PTM aPtm) {
        if (profile.getFixedModifications().contains(aPtm.getName())) {
            profile.removeFixedModification(aPtm.getName());
        } else {
            profile.removeVariableModification(aPtm.getName());
        }
    }

}
