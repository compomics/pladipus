/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue.jmx;

import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.sun.mail.iap.ConnectionException;
import java.io.IOException;
import java.util.HashMap;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;

/**
 *
 * @author Kenneth
 */
public class JMXQueueConnectionCache extends HashMap<CompomicsQueue, QueueViewMBean> {

    /**
     * The activeMQ domain
     */
    private final String amqDomain = "org.apache.activemq";
    //TODO CHECK IF IT WORKS IF WE CHANGE THIS TO "PLADIPUS"
    /**
     * The Specified brokername
     */
    private final String brokerName = "localhost";
    private MBeanServerConnection conn;

    private static JMXQueueConnectionCache cache;

    private JMXQueueConnectionCache() throws IOException, ConnectionException {
        initConnection();
    }

    public static JMXQueueConnectionCache getInstance() throws IOException, ConnectionException {
        if (cache == null) {
            cache = new JMXQueueConnectionCache();
        }
        return cache;
    }

    public QueueViewMBean get(CompomicsQueue queue) throws MalformedObjectNameException {
        QueueViewMBean qvmb;
        if (!containsKey(queue)) {
            qvmb = getNewQueueViewBean(queue);
            put(queue, qvmb);
        }
        return super.get(queue);
    }

    private QueueViewMBean getNewQueueViewBean(CompomicsQueue queue) throws MalformedObjectNameException {
        ObjectName activeMQ = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost");
        BrokerViewMBean mbean = (BrokerViewMBean) MBeanServerInvocationHandler.newProxyInstance(conn, activeMQ, BrokerViewMBean.class, true);

        for (ObjectName name : mbean.getQueues()) {
            QueueViewMBean queueMbean = (QueueViewMBean) MBeanServerInvocationHandler.newProxyInstance(conn, name, QueueViewMBean.class, true);
            if (queueMbean.getName().equals(queue.getQueueName())) {

                return queueMbean;
            }
        }
        throw new IllegalArgumentException("Queue does not exist!");
    }

    private MBeanServerConnection initConnection() throws IOException, ConnectionException {
        if (conn == null) {
            JMXServiceURL url = new JMXServiceURL(NetworkProperties.getInstance().getActiveMQJMXLocation());//new JMXServiceURL("service:jmx:rmi:///jndi/rmi://157.193.229.78:1099/jmxrmi");
            JMXConnector jmxc = JMXConnectorFactory.connect(url);
            conn = jmxc.getMBeanServerConnection();
        }
        return conn;
    }

}
