package com.compomics.pladipus.core.control.engine.impl;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.control.distribution.service.ChainService;
import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.util.ClientNameResolver;
import com.compomics.pladipus.core.model.exception.BrokenProcessChainException;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.PladipusTrafficException;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.exception.XMLInterpreterException;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class SessionProcessingEngine extends ProcessingEngine {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(SessionProcessingEngine.class);
    
    public SessionProcessingEngine(Message message) throws JMSException, IOException {
        super(message);
    }
    
    @Override
    public Object call() throws UnspecifiedPladipusException {
        try {
            return execute();
        } catch (Throwable ex) {
            throw new UnspecifiedPladipusException(ex);
        }
    }
    
    private boolean execute() throws PladipusTrafficException, BrokenProcessChainException, PladipusProcessingException, XMLInterpreterException, ProcessStepInitialisationException, UnspecifiedPladipusException {
        boolean jobSucessfull = false;
        String text;
        try {
            text = getCurrentMessage().getText();
            ProcessingJob aJob = XMLJobInterpreter.getInstance().convertXMLtoJob(text);
            if (!aJob.allowRun()) {
                throw new RejectedExecutionException("This machine is not qualified to run this task");
            } else if (aJob.isMandatoryOrderCheck()) {
                ChainService service = ChainService.getInstance();
                boolean canExecute = service.canExecute(aJob.getChainID(), (int) aJob.getId());
                if (!canExecute) {
                    throw new BrokenProcessChainException("This chain is waiting for a previous process to finish");
                } else {
                    boolean registered = service.registerMe((int) aJob.getId(), ClientNameResolver.getClientIdentifier());
                    if (!registered) {
                        throw new RejectedExecutionException("Worker could not register for this chain of processes");
                    }
                }
                LOGGER.info("Executing job...");
                for (ProcessingStep aStep : aJob) {
                    try {
                        aStep.setProcessingID((int) aJob.getId());
                        aStep.getCallbackNotifier().onNotification(aStep.getDescription(), false);
                        jobSucessfull = aStep.doAction();
                        aStep.getCallbackNotifier().onNotification(aStep.getDescription(), true);
                    } finally {
                        aStep.close();
                    }
                }
                aJob.get(0).getCallbackNotifier().onNotification("Finished", true);
                //sleep 10 seconds to avoid accidental DDOS of servers and resources
                Thread.sleep(10000);
            }
        } catch (ParserConfigurationException | SAXException | JMSException | InterruptedException | SQLException | IOException ex) {
            throw new PladipusTrafficException(ex);
        }
        return jobSucessfull;
    }
    
}
