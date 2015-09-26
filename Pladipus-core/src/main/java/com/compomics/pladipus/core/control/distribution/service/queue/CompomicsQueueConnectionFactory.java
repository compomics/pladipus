/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue;

import com.compomics.pladipus.core.control.util.ClientNameResolver;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class CompomicsQueueConnectionFactory {

    /**
     * The connection factory for activeMQ connections
     */
    private final ActiveMQConnectionFactory connectionFactory;
    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CompomicsQueueConnectionFactory.class);
    /**
     * The Connection object
     */

    private Connection connection;
    /**
     * The Queue connection factory instance
     */

    private static CompomicsQueueConnectionFactory queueConnectionFactory;
    /**
     * The current session (only one per machine)
     */

    private Session session;
    /**
     * The current session for system updates (only one per machine)
     */

    private Session systemSession;
    /**
     * A durable consumer to the system management queue
     */
    private TopicSubscriber systemConsumer;
    /**
     * A regular consumer for job messages
     */
    private MessageConsumer jobConsumer;

    private CompomicsQueueConnectionFactory() throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory(NetworkProperties.getInstance().getActiveMQLocation());
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

        connection = getConnection();
        connection.setClientID(ClientNameResolver.getClientIdentifier());

        systemSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        // create the Topic from which messages will be received
        Topic topic = systemSession.createTopic(CompomicsQueue.UPDATE.getQueueName());
        // create a MessageConsumer for receiving messages
        systemConsumer = systemSession.createDurableSubscriber(topic, ClientNameResolver.getClientIdentifier());
        // Create the destination  queue
        Destination destination = session.createQueue(CompomicsQueue.JOB.getQueueName());
        // Create a MessageConsumer from the Session to the Topic or Queue
        jobConsumer = session.createConsumer(destination);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }

    /**
     *
     * @return the CompomicsQueueConnectionFactory instance
     * @throws JMSException
     */
    public static CompomicsQueueConnectionFactory getInstance() throws JMSException {
        if (queueConnectionFactory == null) {
            queueConnectionFactory = new CompomicsQueueConnectionFactory();
        }
        queueConnectionFactory.getConnection().start();
        return queueConnectionFactory;
    }

    public static void reset() {
        if (queueConnectionFactory != null) {
            queueConnectionFactory.close();
            queueConnectionFactory = null;
        }
    }

    /**
     *
     * @return a connection object to tje JMX queue
     */
    public Connection getConnection() throws JMSException {
        if (connection == null) {
            connection = connectionFactory.createConnection();
        }
        return connection;
    }

    private TopicSubscriber getUpdateConsumer() {
        return systemConsumer;
    }

    private MessageConsumer getJobConsumer() {
        return jobConsumer;
    }

    /**
     *
     * @return the current JMX session object
     */
    public Session getSession() {
        return session;
    }

    /**
     *
     * @param queue the target queue
     * @return a MessageConsumer (regular for Job queue, durable for Update
     * queue)
     */
    public MessageConsumer getConsumer(CompomicsQueue queue) {
        MessageConsumer consumer = null;
        if (queue.equals(CompomicsQueue.JOB)) {
            consumer = getJobConsumer();
        } else if (queue.equals(CompomicsQueue.UPDATE)) {
            consumer = getUpdateConsumer();
        }
        return consumer;
    }

    /**
     * Ensures a clean shut down of the connectionfactory and all contained
     * consumers / connections
     */
    public void close() {
        try {
            systemConsumer.close();
        } catch (JMSException ex) {
            LOGGER.error(ex);
            systemConsumer = null;
        }
        try {
            jobConsumer.close();
        } catch (JMSException ex) {
            LOGGER.error(ex);
            jobConsumer = null;
        }
        try {
            session.close();
        } catch (JMSException ex) {
            LOGGER.error(ex);
            session = null;
        }
        try {
            systemSession.close();
        } catch (JMSException ex) {
            LOGGER.error(ex);
            systemSession = null;
        }
        try {
            connection.close();
        } catch (JMSException ex) {
            LOGGER.error(ex);
            connection = null;
        }
    }

}
