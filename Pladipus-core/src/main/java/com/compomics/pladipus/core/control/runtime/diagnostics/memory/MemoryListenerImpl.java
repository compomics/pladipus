/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.runtime.diagnostics.memory;

import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class MemoryListenerImpl implements MemoryListener {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(MemoryListener.class);

    @Override
    public void checkMemoryUsage(long usedMemory, long maxMemory) {
        double percentageUsed = ((double) usedMemory) / maxMemory;
        if (percentageUsed > MemoryWarningSystem.getPercentageUsageThreshold()) {
            LOGGER.warn("Memory usage is high :" + percentageUsed + "%");
        }
    }
}
