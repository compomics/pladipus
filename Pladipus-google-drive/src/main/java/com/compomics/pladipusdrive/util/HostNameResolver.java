/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipusdrive.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;

/**
 *
 * @author JavaSaurus <javasaurusdev@gmail.com>
 */
public class HostNameResolver {

    private static final Logger LOGGER = Logger.getLogger(HostNameResolver.class);

    public static String getHostName() {
        InetAddress myHost;
        String hostName = "";
        try {
            myHost = InetAddress.getLocalHost();
            hostName = myHost.getCanonicalHostName();
        } catch (UnknownHostException ex) {
            LOGGER.error(ex);
        }
        if (hostName.isEmpty()) {
            hostName = "Pladipus_Worker_Unknown";
        }
        return hostName;
    }
}
