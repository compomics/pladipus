package com.compomics.pladipus.core.control.engine;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.engine.callback.ProcessingMonitor;
import com.compomics.pladipus.core.model.exception.BrokenProcessChainException;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.PladipusTrafficException;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.exception.XMLInterpreterException;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

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
    private ProcessingMonitor monitor;

    public ProcessingEngine() {

    }

    public void stopProcess() {
        if (monitor != null) {
            monitor.stopProcess();
        }
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
     */
    public int startProcess(File executable, List<String> arguments) {
        CallbackNotifier callbackNotifier = new CallbackNotifier(-1);
        return startProcess(executable, arguments, callbackNotifier);
    }

    /**
     *
     * @param executable the jar that should be started on this jvm
     * @param arguments array of arguments + values required to start the jar
     * @return the system exit value of the process
     */
    public int startProcess(File executable, String[] arguments) {
        CallbackNotifier callbackNotifier = new CallbackNotifier(-1);
        return startProcess(executable, arguments, callbackNotifier);
    }

    /**
     *
     * @param executable the executable that should be started on this jvm
     * @param arguments list of arguments + values required to start the jar
     * @param callbackNotifier the notifier to pipe output to
     * @return the system exit value of the process
     */
    public int startProcess(File executable, List<String> arguments, CallbackNotifier callbackNotifier) {
        try {
            monitor = getPreparedMonitor(executable, arguments, callbackNotifier);
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
     * @param arguments array of arguments + values required to start the jar
     * @param callbackNotifier the notifier to pipe output to
     * @return the system exit value of the process
     */
    public int startProcess(File executable, String[] arguments, CallbackNotifier callbackNotifier) {
        try {
            monitor = getPreparedMonitor(executable, arguments, callbackNotifier);
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
    public int startProcess(File executable, List<String> arguments, CallbackNotifier callbackNotifier, Collection<String> errorTerms) throws IOException, InterruptedException, ExecutionException {
        monitor = getPreparedMonitor(executable, arguments, callbackNotifier);
        monitor.addErrorTerms(errorTerms);
        monitor.getHook();
        return 0;
    }

    /**
     *
     * @param executable the executable that should be started on this jvm
     * @param arguments array of arguments + values required to start the jar
     * @param callbackNotifier the notifier to pipe output to
     * @param errorTerms collection of terms that are specificly to be throwing
     * an exception
     * @return the system exit value of the process
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int startProcess(File executable, String[] arguments, CallbackNotifier callbackNotifier, Collection<String> errorTerms) throws IOException, InterruptedException, ExecutionException {
        monitor = getPreparedMonitor(executable, arguments, callbackNotifier);
        monitor.addErrorTerms(errorTerms);
        monitor.getHook();
        return 0;
    }

    private ProcessingMonitor getPreparedMonitor(File executable, List<String> arguments, CallbackNotifier callbackNotifier) {
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(executable.getParentFile());
        LOGGER.info(arguments.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", " "));
        return new ProcessingMonitor(processBuilder, callbackNotifier);
    }

    private ProcessingMonitor getPreparedMonitor(File executable, String[] args, CallbackNotifier callbackNotifier) {
        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        return getPreparedMonitor(executable, arguments, callbackNotifier);
    }

    /**
     *
     * @param aJob, the processingjob that has to be run
     * @return a boolean indicating wether the job was succesfull;
     * @throws Exception
     */
    public boolean runJob(ProcessingJob aJob) throws PladipusProcessingException {
        for (ProcessingStep aStep : aJob) {
            try {
                aStep.getCallbackNotifier().onNotification(aStep.getDescription(), false);
                aStep.doAction();
                aStep.getCallbackNotifier().onNotification(aStep.getDescription(), true);
            } catch (Exception e) {
                //     e.printStackTrace();
                throw new PladipusProcessingException(e);
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
     * @throws
     * com.compomics.pladipus.core.model.exception.PladipusProcessingException
     * @throws
     * com.compomics.pladipus.core.model.exception.XMLInterpreterException
     * @throws
     * com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException
     * @throws
     * com.compomics.pladipus.core.model.exception.PladipusTrafficException
     */
    public boolean runJob(TextMessage aJobMessage) throws PladipusProcessingException, XMLInterpreterException, ProcessStepInitialisationException, PladipusTrafficException {
        try {
            return runJob(XMLJobInterpreter.getInstance().convertXMLtoJob(aJobMessage.getText()));
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            throw new XMLInterpreterException(ex);
        } catch (JMSException ex) {
            throw new PladipusTrafficException(ex);
        }
    }

    @Override
    public Object call() throws UnspecifiedPladipusException {
        try {
            return runJob(currentMessage);
        } catch (Throwable e) {
            throw new UnspecifiedPladipusException(e);
        }
    }

}
