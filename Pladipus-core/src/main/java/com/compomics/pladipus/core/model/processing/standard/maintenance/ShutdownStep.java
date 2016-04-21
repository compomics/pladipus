package com.compomics.pladipus.core.model.processing.standard.maintenance;

import com.compomics.pladipus.core.model.processing.ProcessingStep;

/**
 *
 * @author Kenneth Verheggen
 */
public class ShutdownStep extends ProcessingStep {

    @Override
    public boolean doAction() throws Exception {
        stopProcess();
        System.exit(0);
        return true;
    }

    @Override
    public String getDescription() {
        return "A STEP TO SHUT DOWN THE PLADIPUS INFRASTRUCTURE";
    }

}
