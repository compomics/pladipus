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
public class MakeBlastDbStep extends ProcessingStep {

    public MakeBlastDbStep() {

    }

    @Override
    public boolean doAction() throws Exception {
        File queryFasta = new File(parameters.get("query"));
        File dbFasta = new File(parameters.get("db"));
        String blastType = parameters.get("dbtype");
        makeBlastDb(blastType, queryFasta, getBlastExecutable());
        makeBlastDb(blastType, dbFasta, getBlastExecutable());
        return true;
    }

    private File getBlastExecutable() {
        File makeBlastDb = new File(parameters.get("blast_folder"), "makeblastdb");
        if (!makeBlastDb.exists()) {
            makeBlastDb = new File(parameters.get("blast_folder"), "makeblastdb.exe");
        }
        return makeBlastDb;
    }

    private void makeBlastDb(String blastType, File fasta, File executable) throws IOException, InterruptedException, ExecutionException {
        //make arguments
        ArrayList<String> commands = new ArrayList<>();
        commands.add(executable.getName());
        commands.add("-dbtype");
        commands.add(blastType);
        commands.add("-in");
        commands.add(fasta.getAbsolutePath());
        new ProcessingEngine().startProcess(executable, commands, getCallbackNotifier());
    }

    @Override
    public String getDescription() {
        return "Creating a blast db out of the provided input...";
    }

}
