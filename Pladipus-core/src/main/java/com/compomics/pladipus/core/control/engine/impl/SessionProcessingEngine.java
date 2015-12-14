/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

        String text = getCurrentMessage().getText();
        ProcessingJob aJob = XMLJobInterpreter.getInstance().convertXMLtoJob(text);
        if (!aJob.allowRun()) {
            throw new RejectedExecutionException("This machine is not qualified to run this task");
        } else {
            LOGGER.info("Executing job...");
            for (ProcessingStep aStep : aJob) {
                aStep.setProcessingID((int) aJob.getId());
                aStep.getCallbackNotifier().onNotification(aStep.getDescription(), false);
                if (!aStep.doAction()) {
                    return false;
                }
                aStep.getCallbackNotifier().onNotification(aStep.getDescription(), true);
            }
            aJob.get(0).getCallbackNotifier().onNotification("Finished", true);
            return true;
        }
    }

}
