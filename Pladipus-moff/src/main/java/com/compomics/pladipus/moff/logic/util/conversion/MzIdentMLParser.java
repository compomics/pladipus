package com.compomics.pladipus.moff.logic.util.conversion;

import com.compomics.util.general.CommandLineParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Protein;

/**
 * This class parses the extended output from PeptideShaker into a MOFF
 * compatible tsv file
 *
 * @author Kenneth Verheggen
 */
public class MzIdentMLParser {

    private static final String P_HEADER
            = "prot\t"
            + "peptide\t"
            + "var_mod\t"
            + "fix_mod\t"
            + "spectrum_file\t"
            + "Spectrum_title\t"
            + "spectrum_scan_number\t"
            + "rt\t"
            + "mz\t"
            + "charge\t"
            + "identification_charge\t"
            + "mass\t"
            + "isotope_number\t"
            + "prec_m/z_error[ppm]\t"
            + "decoy\t"
            + "localization_confidence\t"
            + "probabilistic_PTM_score\t"
            + "d-score\t"
            + "confidence\t"
            + "validation";

    /**
     * This constructor takes the PS output file. This has to be created by
     * command line option -reports 9 !
     *
     * @param spectrumFiles the spectrum files associated with the mzidentml
     * @param mzIdentMlFile the mzidentml output file
     * @param moffFile the moff file output
     * @throws java.io.FileNotFoundException
     */
    public static void convertReport(File psOutputFile, List<File> spectrumFiles, File moffFile) throws FileNotFoundException, IOException {
        MzIdentMLControllerImpl mzIdentMLControllerImpl = new MzIdentMLControllerImpl(psOutputFile, true);
        mzIdentMLControllerImpl.addMSController(spectrumFiles);
        FileWriter out = new FileWriter(moffFile);
        out.append("prot" + "\t" + "peptide" + "\t" + "rt" + "\t" + "mz" + "\t" + "mass" + "\t" + "charge").append(System.lineSeparator());
        for (Comparable aProtein : mzIdentMLControllerImpl.getProteinIds()) {
            Protein currentIdentifiedProtein = mzIdentMLControllerImpl.getProteinById(aProtein);
            for (Comparable aPeptide : mzIdentMLControllerImpl.getPeptideIds(aProtein)) {
                Peptide currentIdentifiedPeptide = mzIdentMLControllerImpl.getPeptideByIndex(aProtein, aPeptide);
                //peptide
                String peptide = currentIdentifiedPeptide.getSequence();
                //prot
                String protein = currentIdentifiedProtein.getDbSequence().getAccession();
                if (!protein.contains("REVERSED_")) {
                    //rt
                    String rt = currentIdentifiedPeptide.getSpectrumIdentification().getRetentionTime();
                    //mz
                    double mz = currentIdentifiedPeptide.getSpectrumIdentification().getExperimentalMassToCharge();
                    //mass
                    double mass = currentIdentifiedPeptide.getSpectrumIdentification().getChargeState() * mz;
                    //charge
                    int charge = currentIdentifiedPeptide.getSpectrumIdentification().getChargeState();
                    //print as line
                    out.append(
                            protein + "\t"
                            + peptide + "\t"
                            + rt + "\t"
                            + mz + "\t"
                            + mass + "\t"
                            + charge).append(System.lineSeparator()).flush();
                }
            }
        }
        mzIdentMLControllerImpl.clearMSControllers();
        mzIdentMLControllerImpl.close();
        out.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        Options options = new Options();
        options.addOption("in", true, "input identifications file (.mzid)");
        options.addOption("out", true, "output MoFF formatted file");
        options.addOption("spectrum_files", true, "comma separated list of spectrum files");
        GnuParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);
        File mzIdentFile = new File(cmd.getOptionValue("in"));
        File outputFile = new File(cmd.getOptionValue("out"));
        String[] spectrumFilePaths = cmd.getOptionValues("spectrum_files");
        List<File> spectrumFileList = new ArrayList<File>();
        for (String aSpectrumFilePath : spectrumFilePaths) {
            spectrumFileList.add(new File(aSpectrumFilePath));
        }
        MzIdentMLParser.convertReport(mzIdentFile, spectrumFileList, outputFile);
    }

}
