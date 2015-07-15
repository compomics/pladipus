/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing;

import java.util.HashMap;

/**
 *
 * @author Kenneth Verheggen
 */
public abstract class ProcessingStep implements ProcessingExecutable {

    /**
     * The Logger Instance
     */

    protected HashMap<String, String> parameters;
    /**
     * The fully defined class name of the processing step
     */

    protected String processingStepClassName;

    public String getProcessingStepClassName() {
        return processingStepClassName;
    }

    public void setProcessingStepClassName(String processingStepClassName) {
        this.processingStepClassName = processingStepClassName;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public HashMap<String, String> getParameters() {
        return parameters;
    }

}
