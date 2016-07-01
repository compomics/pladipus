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
public class MakeBlastDbStep extends ProcessingStep {

    public MakeBlastDbStep() {

    }

    @Override
    public boolean doAction() throws UnspecifiedPladipusException {
        try {
            File queryFasta = new File(parameters.get("query"));
            File dbFasta = new File(parameters.get("db"));
            String blastType = parameters.get("dbtype");
            makeBlastDb(blastType, queryFasta, getBlastExecutable());
            makeBlastDb(blastType, dbFasta, getBlastExecutable());
            return true;
        } catch (IOException | InterruptedException | ExecutionException ex) {
            throw new UnspecifiedPladipusException(ex);
        }
    }

    private File getBlastExecutable() {
        File makeBlastDb = new File(parameters.get("blast_folder"), "makeblastdb");
        if (!makeBlastDb.exists()) {
            makeBlastDb = new File(makeBlastDb.getAbsolutePath() + ".exe");
        }
        return makeBlastDb;
    }

    private void makeBlastDb(String blastType, File fasta, File executable) throws IOException, InterruptedException, ExecutionException {
        //make arguments
        ArrayList<String> commands = new ArrayList<>();
        commands.add(executable.getAbsolutePath());
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

    public static void main(String[] args) {
        ProcessingStep.main(args);
    }

}
