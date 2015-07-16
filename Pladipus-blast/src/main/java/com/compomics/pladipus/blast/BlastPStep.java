/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.blast;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Kenneth Verheggen
 */
public class BlastPStep extends ProcessingStep {

    public BlastPStep() {

    }

    @Override
    public boolean doAction() throws Exception {
        File queryFasta = new File(parameters.get("tempQueryFasta"));
        File dbFasta = new File(parameters.get("tempDbFasta"));
        File outputFile = new File(parameters.get("output"));
        executeProteinBlast(queryFasta, dbFasta, outputFile, getBlastExecutable());
        return true;
    }
//blastp -query C:\Users\Kenneth\Desktop\PladipusFTP\data\fasta\HUMAN_concatenated_target_decoy.fasta -db C:\Users\Kenneth\Desktop\PladipusFTP\data\fasta\HUMAN_concatenated_target_decoy.fasta

    public File getBlastExecutable() {
        return new File(parameters.get("blastFolder"), "blastp");
    }

    private void executeProteinBlast(File queryFasta, File dbFasta, File outputFile, File executable) throws IOException, InterruptedException, ExecutionException {
        //make arguments
        ArrayList<String> commands = new ArrayList<>();
        commands.add(executable.getName());
        commands.add("-query");
        commands.add(queryFasta.getAbsolutePath());
        commands.add("-db");
        commands.add(dbFasta.getAbsolutePath());
        commands.add("-out");
        commands.add(outputFile.getAbsolutePath());
        ProcessingEngine.startProcess(executable, commands);
    }

    @Override
    public String getDescription() {
        return "Protein blast";
    }

}
