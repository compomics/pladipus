/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.runtime.diagnostics.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Collection;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import org.apache.log4j.Logger;

/**
 * This memory warning system will call the listener when we exceed the
 * percentage of available memory specified. There should only be one instance
 * of this object created, since the usage threshold can only be set to one
 * number.
 */
public class MemoryWarningSystem {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(MemoryWarningSystem.class);
    /**
     * The Maximal allowed memory
     */
    private static final int allowedMemory = (int) (0.9 * (((com.sun.management.OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean()).getTotalPhysicalMemorySize()) / 1024 / 1024);
    /**
     * The memory threshold for warnings
     */
    private static long warningThreshold;

    /**
     * The collection of listeners for memory (in case more than one)
     */
    private static final Collection<MemoryListener> listeners = new ArrayList<>();
     /**
     * The bean pool to connect to
     */
    private static final MemoryPoolMXBean tenuredGenPool = findTenuredGenPool();

    /**
     *
     * @param usageThreshold the memory threshold
     */
    public MemoryWarningSystem(double usageThreshold) {
        LOGGER.debug("Started memory monitoring");
        MemoryWarningSystem.setPercentageUsageThreshold(usageThreshold);
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        NotificationEmitter emitter = (NotificationEmitter) mbean;
        emitter.addNotificationListener(new NotificationListener() {
            @Override
            public void handleNotification(Notification n, Object hb) {
                if (n.getType().equals(
                        MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                    long maxMemory = tenuredGenPool.getUsage().getMax();
                    long usedMemory = tenuredGenPool.getUsage().getUsed();
                    for (MemoryListener listener : listeners) {
                        listener.checkMemoryUsage(usedMemory, maxMemory);
                    }
                }
            }
        }, null, null);
    }

    /**
     *
     * @return the maximum amount of RAM that can be used
     */
    public static int getAllowedRam() {
        return allowedMemory;
    }

    /**
     * Adds a memory listener to the system
     * @param listener
     * @return success
     */
    public boolean addListener(MemoryListener listener) {
        return listeners.add(listener);
    }
/**
     * Removes a memory listener to the system
     * @param listener
     * @return success
     */
    public boolean removeListener(MemoryListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Sets the memory threshold in percent
     * @param percentage
     */
    public static void setPercentageUsageThreshold(double percentage) {
        if (percentage <= 0.0 || percentage > 1.0) {
            throw new IllegalArgumentException("Percentage not in range");
        }
        long maxMemory = tenuredGenPool.getUsage().getMax();
        warningThreshold = (long) (maxMemory * percentage);
        tenuredGenPool.setUsageThreshold(warningThreshold);
    }

    /**
     *
     * @return the current warningThreshold
     */
    public static double getPercentageUsageThreshold() {
        return warningThreshold;
    }

    /**
     * Tenured Space Pool can be determined by it being of type HEAP and by it
     * being possible to set the usage threshold.
     */
    private static MemoryPoolMXBean findTenuredGenPool() {
        for (MemoryPoolMXBean pool
                : ManagementFactory.getMemoryPoolMXBeans()) {
            // I don't know whether this approach is better, or whether
            // we should rather check for the pool name "Tenured Gen"?
            if (pool.getType() == MemoryType.HEAP
                    && pool.isUsageThresholdSupported()) {
                return pool;
            }
        }
        throw new AssertionError("Could not find tenured space");
    }
}
