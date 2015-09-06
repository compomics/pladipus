/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.engine;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.control.engine.callback.ProcessingMonitor;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessingEngine implements Callable {

    /**
     * the message that is being processed
     */
    private TextMessage currentMessage;
    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(ProcessingEngine.class);

    public ProcessingEngine() {

    }

    /**
     *
     * @param message the message to process
     * @throws JMSException
     * @throws IOException
     */
    public ProcessingEngine(Message message) throws JMSException, IOException {
        this.currentMessage = (TextMessage) message;
    }

    /**
     *
     * @return the message that's being processed
     */
    public TextMessage getCurrentMessage() {
        return currentMessage;
    }

    /**
     *
     * @param jar the jar that should be started on this jvm
     * @param arguments list of arguments + values required to start the jar
     * @return the system exit value of the process
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static int startProcess(File jar, List<String> arguments) throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(jar.getParentFile());
        System.out.println("Launching process @ " + processBuilder.directory().getAbsolutePath());
        return ProcessingMonitor.getHook(processBuilder);
    }

    /**
     *
     * @param aJob, the processingjob that has to be run
     * @return a boolean indicating wether the job was succesfull;
     * @throws Exception
     */
    public boolean runJob(ProcessingJob aJob) throws Exception {
        for (ProcessingStep aStep : aJob) {
            aStep.doAction();
        }
        LOGGER.info("Done !");
        return true;
    }

    /**
     *
     * @param aJobMessage, the text representation of a processingjob that has
     * to be run
     * @return a boolean indicating wether the job was succesfull;
     * @throws Exception
     */
    public boolean runJob(TextMessage aJobMessage) throws Exception {
        ProcessingJob aJob = XMLJobInterpreter.getInstance().convertXMLtoJob(aJobMessage.getText());
        for (ProcessingStep aStep : aJob) {
            aStep.doAction();
        }
        LOGGER.info("Done !");
        return true;
    }

    @Override
    public Object call() throws Exception {
        return runJob(currentMessage);
    }

}
