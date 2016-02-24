package com.compomics.pladipus.core.control.engine;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.engine.callback.ProcessingMonitor;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
     * @param executable the jar that should be started on this jvm
     * @param arguments list of arguments + values required to start the jar
     * @return the system exit value of the process
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int startProcess(File executable, List<String> arguments) {
        CallbackNotifier callbackNotifier = new CallbackNotifier(-1);
        return startProcess(executable, arguments, callbackNotifier);
    }

    /**
     *
     * @param executable the executable that should be started on this jvm
     * @param arguments list of arguments + values required to start the jar
     * @param callbackNotifier the notifier to pipe output to
     * @return the system exit value of the process
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int startProcess(File executable, List<String> arguments, CallbackNotifier callbackNotifier) {
        try {
            ProcessingMonitor monitor = getPreparedMonitor(executable, arguments, callbackNotifier);
            monitor.getHook();
        } catch (Exception ex) {
            LOGGER.error(ex);
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     *
     * @param executable the executable that should be started on this jvm
     * @param arguments list of arguments + values required to start the jar
     * @param callbackNotifier the notifier to pipe output to
     * @param errorTerms collection of terms that are specificly to be throwing
     * an exception
     * @return the system exit value of the process
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int startProcess(File executable, List<String> arguments, CallbackNotifier callbackNotifier, Collection<String> errorTerms) {
        try {
            ProcessingMonitor monitor = getPreparedMonitor(executable, arguments, callbackNotifier);
            monitor.addErrorTerms(errorTerms);
            monitor.getHook();
        } catch (Exception ex) {
            LOGGER.error(ex);
            ex.printStackTrace();
        }
        return 0;
    }

    private ProcessingMonitor getPreparedMonitor(File executable, List<String> arguments, CallbackNotifier callbackNotifier) {
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(executable.getParentFile());
        LOGGER.info("Launching process @ " + processBuilder.directory().getAbsolutePath());
        LOGGER.info(arguments.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", " "));
        return new ProcessingMonitor(processBuilder, callbackNotifier);
    }

    /**
     *
     * @param aJob, the processingjob that has to be run
     * @return a boolean indicating wether the job was succesfull;
     * @throws Exception
     */
    public boolean runJob(ProcessingJob aJob) throws Exception {
        for (ProcessingStep aStep : aJob) {
            try {
                aStep.getCallbackNotifier().onNotification(aStep.getDescription(), false);
                aStep.doAction();
                aStep.getCallbackNotifier().onNotification(aStep.getDescription(), true);
            } catch (Exception e) {
                //     e.printStackTrace();
                throw e;
            }
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
        return runJob(XMLJobInterpreter.getInstance().convertXMLtoJob(aJobMessage.getText()));
    }

    @Override
    public Object call() throws Exception {
        return runJob(currentMessage);
    }

}
