/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.util;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class CLIRunner {

    private static final Logger LOGGER = Logger.getLogger(CLIRunner.class);

    private static Options GetOptions(boolean fromFile) {
        Options options = new Options();
        options.addOption("help", false, "Help message on the usage of the commandline");
        options.addOption("fasta", true, "The sequence database to be searched");
        options.addOption("out", true, "The output folder for the results");
        options.addOption("peptideshaker", false, "Indicating if PeptideShaker needs to be run");
        if (fromFile) {
            options.addOption("id_params", true, "The compomics Identification Parameters file");
            options.addOption("spectrum_files", true, "The input spectrum files (MGF)");
        } else {
            options.addOption("assay", true, "The PRIDE assay identifier");
        }
        return options;
    }

    public static void BuildIntegrationCommandLine(String[] args) {
        boolean fromFile = !Arrays.asList(args).contains("-assay");

        BasicParser parser = new BasicParser();

        //parse the command line
        try {
            CommandLine line = parser.parse(GetOptions(fromFile), args);
            String fasta = null;
            File outputFolder = null;
            boolean isValidCLI = true;
            boolean runPeptideShaker = line.hasOption("peptideshaker");
            if (line.hasOption("fasta")) {
                fasta = line.getOptionValue("fasta");
            } else {
                LOGGER.error("Fasta is a mandatory parameter.");
                isValidCLI = false;
            }

            if (line.hasOption("out")) {
                outputFolder = new File(line.getOptionValue("out"));
                if (!outputFolder.exists()) {
                    outputFolder.mkdirs();
                } else if (!outputFolder.isDirectory()) {
                    LOGGER.error("Output folder exists but is not a directory !");
                    isValidCLI = false;
                }
            }

            if (!fromFile) {
                String assay = null;
                if (line.hasOption("assay")) {
                    assay = line.getOptionValue("assay");
                } else {
                    LOGGER.error("Assay is a mandatory parameter in case files are not provided.");
                    isValidCLI = false;
                }
                if (isValidCLI) {
                    Integration.run(assay, fasta, outputFolder, runPeptideShaker);
                }
            } else {
                String spectrum_files = null;
                String id_params = null;
                if (line.hasOption("spectrum_files")) {
                    spectrum_files = line.getOptionValue("spectrum_files");
                } else {
                    LOGGER.error("spectrum_files is a mandatory parameter in case an assay identifier is not provided.");
                    isValidCLI = false;
                }
                if (line.hasOption("id_params")) {
                    id_params = line.getOptionValue("id_params");
                } else {
                    LOGGER.error("id_params is a mandatory parameter in case an assay identifier is not provided.");
                    isValidCLI = false;
                }
                if (isValidCLI) {
                    IntegrationFromFile.run(spectrum_files, id_params, fasta, outputFolder, runPeptideShaker);
                }
            }
        } catch (ParseException ex) {
            LOGGER.error(ex);
        }
    }

    public static void main(String[] args) {
        args = new String[]{
            "-assay", "11954",
            "-out", "C:/Users/compomics/.compomics/pladipus/search/results",
            "-fasta", "C:/Users/compomics/Documents//out/human_proteome_tincr_crap_concatenated_target_decoy.fasta"};
       //     "-peptideshaker"};
        BuildIntegrationCommandLine(args);
    }

}
