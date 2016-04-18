/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing;

import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
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
    /**
     * The id of the current process / job
     */
    private int processingID = -1;
    /**
     * a notifier for the process proceedings
     */
    private CallbackNotifier callbackNotifier;

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

    public int getProcessingID() {
        return processingID;
    }

    public void setProcessingID(int processingID) {
        this.processingID = processingID;
        this.callbackNotifier = new CallbackNotifier(processingID);
    }

    public CallbackNotifier getCallbackNotifier() {
        if (callbackNotifier == null) {
            callbackNotifier = new CallbackNotifier();
        }
        return callbackNotifier;
    }

}
