/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Kenneth Verheggen
 */
public class ClientNameResolver {

    /**
     * boolean indicating if the server should be run in testing mode. This is
     * done because otherwise ActiveMQ does not allow for multiple connections
     * on a single host
     */
    private static boolean testingMode;

    /**
     * Sets the system mode (testing or regular)
     *
     * @param testingMode
     */
    public static void setTestingMode(boolean testingMode) {
        ClientNameResolver.testingMode = testingMode;
    }

    private static String getIPAdress() {
        try {
            String ip = "";
            InetAddress.getLocalHost().getCanonicalHostName();
            return ip;
        } catch (UnknownHostException ex) {
            return "" + System.currentTimeMillis();
        }
    }

    private static String getTestingString() {
        return "Testing_" + System.currentTimeMillis();
    }

    /**
     *
     * @return the unique client's identifier. This is used for durable
     * subscriptions, so ActiveMQ can find the client's. Otherwise, it might
     * miss out on system update broadcasts
     */
    public static String getClientIdentifier() {
        String clientIdentifier;
        if (testingMode) {
            clientIdentifier = getTestingString();
        } else {
            clientIdentifier = getIPAdress();
        }
        return clientIdentifier;
    }

}
