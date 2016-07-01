/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.XMLInterpreter;
import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.runtime.steploader.impl.SpringProcessingStepLoader;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public abstract class ProcessingStep implements ProcessingExecutable, AutoCloseable {

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
    /**
     * a boolean indicating whether the step has finished
     */
    protected boolean isDone = false;
    /**
     * The processing engine for subprocesses
     */
    private ProcessingEngine processingEngine;

    public ProcessingStep() {

    }

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

    public void startProcess(File executable, List<String> constructArguments) {
        processingEngine = new ProcessingEngine();
        processingEngine.startProcess(executable, constructArguments, getCallbackNotifier());
    }

    public void startProcess(File executable, String[] constructArguments) {
        processingEngine = new ProcessingEngine();
        processingEngine.startProcess(executable, constructArguments, getCallbackNotifier());
    }

    public void stopProcess() {
        if (processingEngine != null) {
            processingEngine.stopProcess();
        }
    }

    @Override
    public void close() {
        stopProcess();
        isDone = true;
        //do other stuff that needs to be done to close this step nicely (close streams etc)
    }

    public boolean isIsDone() {
        return isDone;
    }

    private static String getCallerClass() throws ClassNotFoundException {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String rawFQN = stElements[3].toString().split("\\(")[0];
        return (rawFQN.substring(0, rawFQN.lastIndexOf('.')));
    }

    public static void main(String[] args) {
        try {
            HashMap<String, String> parameters = new HashMap<>();
            String currentClassName = getCallerClass();
            ProcessingStep step = loadStepFromClassName(currentClassName);
            System.out.println(step.getDescription());
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    if (i <= args.length - 1 && !args[i + 1].startsWith("-")) {
                        parameters.put(args[i].substring(1), args[i + 1]);
                    } else {
                        parameters.put(args[i], "");
                    }
                }
            }
            step.setParameters(parameters);
            step.doAction();
        } catch (UnspecifiedPladipusException | PladipusProcessingException | ClassNotFoundException | ProcessStepInitialisationException | IOException ex) {
            Logger.getLogger(ProcessingStep.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static ProcessingStep loadStepFromClassName(String className) throws ProcessStepInitialisationException, IOException {
        try {
            Class<?> clazz = Class.forName(className);
            return (ProcessingStep) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException ex) {
            throw new ProcessStepInitialisationException(ex.getMessage());
        }
    }

}
