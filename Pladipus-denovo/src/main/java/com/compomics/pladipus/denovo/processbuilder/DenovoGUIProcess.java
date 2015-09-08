/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.denovo.processbuilder;

import com.compomics.pladipus.core.control.runtime.diagnostics.memory.MemoryWarningSystem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class DenovoGUIProcess {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(DenovoGUIProcess.class);
    /**
     * The PeptideShaker jar file
     */
    private final File denovoGUIJar;
    /**
     * The input folder (= SearchGUI result folder)
     */
    private final File inputFolder;
    /**
     * The search parameters file (used in SearchGUI)
     */
    private final File searchParamFile;
    /**
     * The output dir to be used by the next process
     */
    private File outputDir;

    public DenovoGUIProcess(File outputFolder, File inputFolder, File searchParamFile, File denovoGUIJar) {
        this.inputFolder = inputFolder;
        this.searchParamFile = searchParamFile;
        this.denovoGUIJar = denovoGUIJar;
        this.outputDir = outputFolder;
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
        PSCommandLine.add(getDenovoGUIJar().getAbsolutePath());
        PSCommandLine.add("com.compomics.denovogui.cmd.DeNovoCLI");
        PSCommandLine.add("-spectrum_files");
        PSCommandLine.add(inputFolder.getAbsolutePath());
        PSCommandLine.add("-output_folder");
        PSCommandLine.add(outputDir.getAbsolutePath());
        PSCommandLine.add("-id_params");
        PSCommandLine.add(searchParamFile.getAbsolutePath());

        PSCommandLine.stream().forEach((anArg) -> {
            LOGGER.info(anArg + " ");
        });

        return PSCommandLine;
    }

    public File getDenovoGUIJar() {
        return denovoGUIJar;
    }

    /**
     * sets the output directory
     *
     * @param outputDir
     * @return the process
     */
    public DenovoGUIProcess setOutputFolder(File outputDir) {
        if (outputDir.exists()) {
            outputDir.delete();
        }
        outputDir.mkdirs();
        this.outputDir = outputDir;
        return this;
    }

}
