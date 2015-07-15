/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue;

import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.IOException;
import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public abstract class CompomicsConsumer implements Runnable, ExceptionListener {

    /**
     * The queue to pull xml from
     */
    protected final CompomicsQueue queue;
    /**
     * The current message consumer to handle xmls being pulled from a queue
     */
    protected MessageConsumer consumer;
    /**
     * The Logging instance
     */
    protected static final Logger LOGGER = Logger.getLogger(CompomicsConsumer.class);

    public CompomicsConsumer(CompomicsQueue queue) throws IOException, JMSException {
        this.queue = queue;
        this.consumer = CompomicsQueueConnectionFactory.getInstance().getConsumer(queue);
    }

    /**
     * Attempts to execute the received message (in case of a valid jobXML and
     * configuration)
     *
     * @param message the received message
     * @throws JMSException
     */
    public abstract void processMessage(Message message) throws JMSException;

    @Override
    public void run() {
        try {
            Connection connection = CompomicsQueueConnectionFactory.getInstance().getConnection();
            connection.start();
            Message message = consumer.receive(1000);
            processMessage(message);
            connection.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handles any JMSException that happens pulling from this queue
     * @param ex
     */
    @Override
    public synchronized void onException(JMSException ex) {
        LOGGER.error("A queue related exception occured. '" + ex + ") Shutting down client.");
    }
}
