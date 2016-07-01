package com.compomics.pladipus.core.model.processing.standard;

import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;

public class SweetCountdownStep extends ProcessingStep {

    public SweetCountdownStep() {

    }

    @Override
    public boolean doAction() throws PladipusProcessingException {
        int countDownSize = Integer.parseInt(parameters.getOrDefault("countDownSize", "10"));
        int stepDownSize = Integer.parseInt(parameters.getOrDefault("stepDownSize", "1"));

        for (int i = countDownSize; i > 0; i -= stepDownSize) {
            System.out.println(i);
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "Executing some really fancy countdown code !";
    }

    public static void main(String[] args) {
        ProcessingStep.main(args);
    }
}
