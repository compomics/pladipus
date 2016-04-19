package com.compomics.moff.gui.control.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class parses the extended output from PeptideShaker into a MOFF
 * compatible tsv file
 *
 * @author Kenneth Verheggen
 */
public class PSOutputParser {

    private static final int LINE = 0;
    private static final int PROTEINS = 1;
    private static final int SEQUENCE = 2;
    private static final int VAR_MODS = 3;
    private static final int FIX_MODS = 4;
    private static final int SPECTRUM_FILE = 5;
    private static final int SPECTRUM_TITLE = 6;
    private static final int SPECTRUM_SCAN_NR = 7;
    private static final int RT = 8;
    private static final int MZ = 9;
    private static final int MEASURED_CHARGE = 10;
    private static final int IDENTIFIED_CHARGE = 11;
    private static final int THEORETICAL_MASS = 12;
    private static final int ISOTOPE_NR = 13;
    private static final int PRECURSOR_MZ_ERROR = 14;
    private static final int DECOY = 15;
    private static final int LOC_PTM_CONF = 16;
    private static final int PROB_PTM_SCORE = 17;
    private static final int D_SCORE = 18;
    private static final int CONFIDENCE = 19;
    private static final int VALIDATION = 20;

    private static final String P_HEADER = "prot\t"
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
     * @param psOutputFile the ps output file
     * @param moffFile the moff file output
     * @throws java.io.FileNotFoundException
     */
    public static void convertReport(File psOutputFile, File moffFile) throws FileNotFoundException, IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(psOutputFile)); BufferedWriter writer = new BufferedWriter(new FileWriter(moffFile));) {
            String line;
            //peptideshaker headers
            reader.readLine();
            writer.append(P_HEADER).append(System.lineSeparator());
            //content
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                if (!line.isEmpty()) {
                    String[] split = line.split("\t");
                    if (split.length > 1) {
                        //filter out shared peptides and decoys
                        if (!split[PROTEINS].contains(",") && split[DECOY].equals("0")) {
                            //write the requested line to file
                            writer.append(line.substring(line.indexOf("\t") + 1)).append(System.lineSeparator()).flush();
                        }
                    }
                }
            }
            writer.flush();
        }
    }

}
