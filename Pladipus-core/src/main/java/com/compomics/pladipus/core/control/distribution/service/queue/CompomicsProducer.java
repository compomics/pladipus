/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue;

import com.compomics.pladipus.core.control.util.ClientNameResolver;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
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
     * This producer's connection (only one per machine)
     */
    private Connection connection;
    /**
     * This producer's session (only one per machine)
     */
    private Session session;
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
     * The collection of messages to send
     */
    private HashMap<String, Integer> messages = new HashMap<>();

    /**
     *
     * @param queue the queue this producer will push to
     * @param message the textual message that should be send
     * @param priority the priority level of this producer (0-9, default = 4)
     * @throws IOException
     * @throws JMSException
     */
    public CompomicsProducer(CompomicsQueue queue, int priority) throws IOException, JMSException {
        init(queue);
        this.priority = priority;
    }

    /**
     *
     * @param queue the queue this producer will push to
     * @throws IOException
     * @throws JMSException
     */
    public CompomicsProducer(CompomicsQueue queue) throws IOException, JMSException {
        init(queue);
    }

    public void addMessage(String message, Integer processId) {
        this.messages.put(message, processId);
    }

    public void addMessage(HashMap<String, Integer> messages) {
        this.messages.putAll(messages);
    }

    private void init(CompomicsQueue queue) throws JMSException, IOException {
        this.queue = queue;
        if (priority > 9) {
            priority = 9;
        } else if (priority < 0) {
            priority = 0;
        }
        initConnection();
    }

    private void initConnection() throws JMSException, JMSException, JMSException, JMSException, JMSException {
        // Create a Connection
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(NetworkProperties.getInstance().getActiveMQLocation());
        connectionFactory.setCloseTimeout(30000);
        connectionFactory.setUseAsyncSend(true);
        // Create a redeliverypolicy
        RedeliveryPolicy queuePolicy = new RedeliveryPolicy();
        queuePolicy.setInitialRedeliveryDelay(0);
        queuePolicy.setRedeliveryDelay(1000);
        queuePolicy.setUseExponentialBackOff(false);
        //TODO make this a property
        queuePolicy.setMaximumRedeliveries(5);
        connectionFactory.setRedeliveryPolicy(queuePolicy);

        connection = connectionFactory.createConnection();
        connection.setClientID(ClientNameResolver.getClientIdentifier());
        // Create a Session
        session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
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
        for (Map.Entry<String, Integer> message : messages.entrySet()) {
            txtMessage = session.createTextMessage(message.getKey());
            txtMessage.setJMSPriority(priority);
            txtMessage.setJMSMessageID(String.valueOf(message.getValue()));
            txtMessage.setJMSCorrelationID(String.valueOf(message.getValue()));
            sendMessage(txtMessage);
        }
        //commit the session
        session.commit();
    }

    private void sendMessage(TextMessage txtMessage) throws JMSException {
        // Tell the producer to send the message
        producer.send(txtMessage);

    }

    /**
     * Ensures a clean shut down of the producer
     */
    @Override
    public void close() {
        LOGGER.debug("Closing producer...");
        try {
            if (producer != null) {
                producer.close();
            }
        } catch (JMSException ex) {
            LOGGER.error(ex);
            producer = null;
        }
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException ex) {
            LOGGER.error(ex);
            session = null;
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ex) {
            LOGGER.error(ex);
            connection = null;
        }

    }

    @Override
    public void run() {
        try {
            if (connection == null) {
                initConnection();
            }
            processMessage();
        } catch (Exception ex) {
            LOGGER.error(ex);
        } finally {
            close();
        }
    }
}
