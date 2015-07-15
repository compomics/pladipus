/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.engine.callback;

import com.compomics.pladipus.core.control.distribution.service.ProcessService;
import java.sql.SQLException;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class CallbackNotifier {

    /**
     * The callback notifier instance
     */
    private static CallbackNotifier notifier;
    /**
     * The a Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CallbackNotifier.class);

    private CallbackNotifier() {

    }

    /**
     *
     * @returns a notifier instance
     */
    public static CallbackNotifier getInstance() {
        if (notifier == null) {
            notifier = new CallbackNotifier();
        }
        return notifier;
    }

    /**
     * Provides feedback to the queue where the task was pulled from.
     *
     * @param event the message to feedback
     * @param message the active MQ session message
     * @throws JMSException
     */
    public void onNotification(String event, Message message) throws SQLException, JMSException {
        ProcessService pService = ProcessService.getInstance();
        int processID = Integer.parseInt(message.getJMSCorrelationID());
        pService.updateProcessState(processID, event);
        LOGGER.info(processID + "\t:" + event);
        LOGGER.debug("Pladipus Controller was notified.");
    }
}
