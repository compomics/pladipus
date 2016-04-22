package com.compomics.pladipus.blast;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class BlastStep extends ProcessingStep {

    public BlastStep() {

    }

    @Override
    public boolean doAction() throws UnspecifiedPladipusException {
        File queryFasta = new File(parameters.get("query"));
        File dbFasta = new File(parameters.get("db"));
        File outputFile = new File(parameters.get("output"));
        try {
            executeProteinBlast(queryFasta, dbFasta, outputFile, getBlastExecutable());
        } catch (IOException | InterruptedException | ExecutionException ex) {
            throw new UnspecifiedPladipusException(ex);
        }
        return true;
    }
//blastp -query C:\Users\Kenneth\Desktop\PladipusFTP\data\fasta\HUMAN_concatenated_target_decoy.fasta -db C:\Users\Kenneth\Desktop\PladipusFTP\data\fasta\HUMAN_concatenated_target_decoy.fasta

    public File getBlastExecutable() {
        return new File(parameters.get("blast_folder"), parameters.get("blast_type").toLowerCase());
    }

    private void executeProteinBlast(File queryFasta, File dbFasta, File outputFile, File executable) throws IOException, InterruptedException, ExecutionException {
        //make arguments
        ArrayList<String> commands = new ArrayList<>();
        commands.add(executable.getAbsolutePath());
        commands.add("-query");
        commands.add(queryFasta.getAbsolutePath());
        commands.add("-db");
        commands.add(dbFasta.getAbsolutePath());
        commands.add("-out");
        commands.add(outputFile.getAbsolutePath());
        commands.add("-outfmt");
        commands.add("6");
        commands.add("-qcov_hsp_perc");
        commands.add("100");
        new ProcessingEngine().startProcess(executable, commands, getCallbackNotifier());
        //copy the output to the correct folder?--> text files are fast, no need
    }

    @Override
    public String getDescription() {
        return "Protein blast";
    }

}
