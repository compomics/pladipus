/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processbuilder;

import com.compomics.pladipus.core.control.runtime.diagnostics.memory.MemoryWarningSystem;
import com.compomics.software.CommandLineUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerProcess {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(PeptideShakerProcess.class);
    /**
     * The PeptideShaker jar file
     */
    private final File peptideShakerJar;
    /**
     * The input folder (= SearchGUI result folder)
     */
    private final File inputFolder;
    /**
     * The search parameters file (used in SearchGUI)
     */
    private final File searchParamFile;
    /**
     * The assayID = the identifier of this search
     */
    private String assayID;
    /**
     * The output dir to be used by the next process
     */
    private File outputDir;

    public PeptideShakerProcess(String AssayID, File inputFolder, File searchParamFile, File peptideShakerJar) {
        this.inputFolder = inputFolder;
        this.searchParamFile = searchParamFile;
        this.peptideShakerJar = peptideShakerJar;
        this.assayID = AssayID;
    }

    /**
     *
     * @return the command line for the process
     * @throws NullPointerException
     */
     public List<String> generateCommand() throws IOException {
        List<String> PSCommandLine = new ArrayList<>();
        PSCommandLine.add("java");
        PSCommandLine.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
        PSCommandLine.add("-cp");
        PSCommandLine.add(getPeptideShakerJar().getAbsolutePath());
        PSCommandLine.add("eu.isas.peptideshaker.cmd.PeptideShakerCLI");
        PSCommandLine.add("-experiment");
        PSCommandLine.add(assayID);
        PSCommandLine.add("-sample");
        PSCommandLine.add("respin");
        PSCommandLine.add("-replicate");
        PSCommandLine.add("0");
        PSCommandLine.add("-identification_files");
        PSCommandLine.add(inputFolder.getAbsolutePath()+"/searchgui_out.zip");
       /* PSCommandLine.add("-spectrum_files");
        PSCommandLine.add(getUsedMGFsFromSearchGUI());
        PSCommandLine.add("-id_params");
        PSCommandLine.add(searchParamFile.getAbsolutePath());*/
        PSCommandLine.add("-out");
        PSCommandLine.add(getResultingCpsFile().getAbsolutePath());
        PSCommandLine.add("-max_precursor_error_type");
        PSCommandLine.add("0");

        PSCommandLine.add("out_txt_1");

        for (String anArg : PSCommandLine) {
            System.out.print(anArg + " ");
        }
        System.out.println();

        return PSCommandLine;
    }

    public File getResultingCpsFile() {
        return new File(inputFolder, assayID + ".cps");
    }

    private String getUsedMGFsFromSearchGUI() throws IOException {
        LineNumberReader reader = null;
        ArrayList<File> mgfFiles = new ArrayList<>();
        File searchGuiInputFile = new File(inputFolder, "searchGUI_input.txt");
        if (searchGuiInputFile.exists()) {
            try {
                reader = new LineNumberReader(new FileReader(searchGuiInputFile));
                String mgfLine;
                while ((mgfLine = reader.readLine()) != null) {
                    mgfFiles.add(new File(mgfLine));
                    LOGGER.debug("Using MGF :" + mgfLine);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    LOGGER.error(ex);
                }
            }
            searchGuiInputFile.delete();
            return CommandLineUtils.getCommandLineArgument(mgfFiles).replace("\"", "");
        } else {
            throw new IOException("No MGF-file could be found that was used by SearchGUI");
        }

    }

    public File getPeptideShakerJar() {
        return peptideShakerJar;
    }

    /**
     * sets the output directory
     * @param outputDir
     * @return the process
     */
    public PeptideShakerProcess setOutputFolder(File outputDir) {
        if (outputDir.exists()) {
            outputDir.delete();
        }
        outputDir.mkdir();
        this.outputDir = outputDir;
        return this;
    }

    /**
     * Sets the process name
     * @param name
     * @return the process
     */
    public PeptideShakerProcess setName(String name) {
        this.assayID = name;
        return this;
    }
}
