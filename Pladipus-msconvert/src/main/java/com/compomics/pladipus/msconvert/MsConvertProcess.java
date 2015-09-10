/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.msconvert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class MsConvertProcess {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(MsConvertProcess.class);
    /**
     * The executable file
     */
    private final File executable;
    /**
     * The input raw file
     */
    private final File input;

    /**
     * The output file
     */
    private File outputFile;

    public MsConvertProcess(File input, File output,File executable) {
        this.input = input;
        this.outputFile=output;
        this.executable = executable;
    }

    /**
     *
     * @return the command line for the process
     * @throws NullPointerException
     */
    public List<String> generateCommand() throws NullPointerException {
        List<String> msConvertCommandLine = new ArrayList<>();
        //set input
        msConvertCommandLine.add("msconvert");
        msConvertCommandLine.add(input.getAbsolutePath());
        //set output
        msConvertCommandLine.add("-o");
        msConvertCommandLine.add(outputFile.getAbsolutePath());
        //display process
        msConvertCommandLine.add("-v");
        //convert to MGF
        msConvertCommandLine.add("--mgf");
        //only retain ms2
        msConvertCommandLine.add("--filter");
        msConvertCommandLine.add("\"msLevel 2\"");
        // do peak_picking
        msConvertCommandLine.add("--filter");
        msConvertCommandLine.add("\"peakPicking true 1-\"");
        //remove zero peaks
        msConvertCommandLine.add("--filter");
        msConvertCommandLine.add("\"zeroSamples removeExtra\"");
        LOGGER.info("MSCONVERT COMMAND LINE : " + System.lineSeparator() + msConvertCommandLine.toString());
        return msConvertCommandLine;
    }

}
