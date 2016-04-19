package com.compomics.pladipus.core.control.engine.impl;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.log4j.Logger;

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
    public Boolean call() throws Exception {
        boolean jobSucessfull = false;
        String text = getCurrentMessage().getText();
        ProcessingJob aJob = XMLJobInterpreter.getInstance().convertXMLtoJob(text);
        if (!aJob.allowRun()) {
            throw new RejectedExecutionException("This machine is not qualified to run this task");
        } else {
            LOGGER.info("Executing job...");
            for (ProcessingStep aStep : aJob) {
                try {
                    aStep.setProcessingID((int) aJob.getId());
                    aStep.getCallbackNotifier().onNotification(aStep.getDescription(), false);
                    jobSucessfull = aStep.doAction();
                    aStep.getCallbackNotifier().onNotification(aStep.getDescription(), true);
                } catch (Exception e) {
                    throw e;
                } finally {
                    aStep.close();
                }
            }
            aJob.get(0).getCallbackNotifier().onNotification("Finished", true);
            //sleep 10 seconds to avoid accidental DDOS of servers and resources
            Thread.sleep(10000);
            return jobSucessfull;
        }
    }

}
