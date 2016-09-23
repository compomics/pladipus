package com.compomics.pladipus.blast;

import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Kenneth Verheggen
 */
public class BlastSetupStep extends ProcessingStep {

    /**
     * the temp folder for the entire processing
     */
    private final Path tempResources = Paths.get(System.getProperty("user.home") + "/pladipus/temp/blast");

    public BlastSetupStep() {

    }

    @Override
    public boolean doAction() throws UnspecifiedPladipusException {
        try {
            Files.createTempDirectory(tempResources,"blast");
            initialize();
            return true;
        } catch (Exception ex) {
            throw new UnspecifiedPladipusException(ex);
        }
    }

    private void initialize() throws Exception {
        //original

        parameters.put("query", Files.copy(Paths.get(parameters.get("query")), tempResources.resolve(Paths.get(parameters.get("query")).getFileName())).toString());

        parameters.put("db", Files.copy(Paths.get(parameters.get("db")), tempResources.resolve(Paths.get(parameters.get("query")).getFileName())).toString());

    }

    @Override
    public String getDescription() {
        return "Initialisation of the blasting process";
    }

    public static void main(String[] args) {
        ProcessingStep.main(args);
    }

}
