/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.playground;

import com.compomics.pladipus.core.control.distribution.PladipusTrafficManager;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsQueueConnectionFactory;
import com.compomics.pladipus.core.control.util.ClientNameResolver;
import javax.jms.JMSException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class ConsumerStarter {

    private static PladipusTrafficManager trafficManager = PladipusTrafficManager.getInstance();
    
    public static void main(String[] args) throws JMSException {
        try {
            while (true) {
                Logger.getRootLogger().setLevel(Level.INFO);
                ClientNameResolver.setTestingMode(false);
                trafficManager.pullFromPladipus();
                Thread.sleep(5000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            CompomicsQueueConnectionFactory.getInstance().close();
        }
    }

}
