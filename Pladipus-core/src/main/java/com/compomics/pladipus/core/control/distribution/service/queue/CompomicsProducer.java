/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue;

import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.IOException;
import java.sql.SQLException;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class CompomicsProducer implements Runnable, AutoCloseable {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CompomicsProducer.class);
    /**
     * The queue to push jobs to
     */
    private CompomicsQueue queue;
    /**
     * The producer that pushes jobs to the queue
     */
    private MessageProducer producer;
    /**
     * This producer's session (only one per machine)
     */
    private Session session;
    /**
     * The message to push
     */
    private String message;
    /**
     * The text representation of the message
     */
    private TextMessage txtMessage;
    /**
     * The processID to push
     */
    private int processID;
    /**
     * The default priority level
     */
    private int priority = 4;

    /**
     *
     * @param queue the queue this producer will push to
     * @param message the textual message that should be send
     * @param processID the process ID that will be pushed
     * @param priority the priority level of this producer (0-9, default = 4)
     * @throws IOException
     * @throws JMSException
     */
    public CompomicsProducer(CompomicsQueue queue, String message, int processID, int priority) throws IOException, JMSException {
        init(queue, message, processID);
        this.priority = priority;
    }

    /**
     *
     * @param queue the queue this producer will push to
     * @param message the textual message that should be send
     * @param processID the process ID that will be pushed
     * @throws IOException
     * @throws JMSException
     */
    public CompomicsProducer(CompomicsQueue queue, String message, int processID) throws IOException, JMSException {
        init(queue, message, processID);
    }

    private void init(CompomicsQueue queue, String message, int processID) throws JMSException, IOException {
        this.queue = queue;
        this.message = message;
        this.processID = processID;
        if (priority > 9) {
            priority = 9;
        } else if (priority < 0) {
            priority = 0;
        }
        initConnection();
    }

    private void initConnection() throws JMSException, JMSException, JMSException, JMSException, JMSException {
        // Create a Connection
        CompomicsQueueConnectionFactory instance = CompomicsQueueConnectionFactory.getInstance();
        //it needs to be a connection WITHOUT the prefetch...
        // Create a Session
        session = instance.getSession();
        // Create the destination (Topic or Queue)
        Destination destination;
        if (queue.isBroadcastToAll()) {
            destination = session.createTopic(queue.getQueueName());
        } else {
            destination = session.createQueue(queue.getQueueName());

        }
        producer = session.createProducer(destination);
        //
        producer.setPriority(priority);
        //
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

    }

    /**
     * Processes the message to be pushed
     *
     * @throws JMSException
     * @throws SQLException
     */
    private void processMessage() throws JMSException, SQLException {
        // Create a messages
        if (txtMessage == null) {
            txtMessage = session.createTextMessage(message);
            txtMessage.setJMSPriority(priority);
            txtMessage.setJMSMessageID(String.valueOf(processID));
            txtMessage.setJMSCorrelationID(String.valueOf(processID));
        }
        sendMessage(txtMessage);
    }

    private void sendMessage(TextMessage txtMessage) throws JMSException {
        // Tell the producer to send the message
        producer.send(txtMessage);
        session.commit();
    }

    /**
     * Ensures a clean shut down of the producer
     */
    @Override
    public void close() {
        try {
            producer.close();
        } catch (JMSException ex) {
            LOGGER.error(ex);
            producer = null;
        }
    }

    @Override
    public void run() {
        try {
            initConnection();
            processMessage();
        } catch (SQLException | JMSException ex) {
            LOGGER.error(ex);
        } finally {
            close();
        }
    }
}
