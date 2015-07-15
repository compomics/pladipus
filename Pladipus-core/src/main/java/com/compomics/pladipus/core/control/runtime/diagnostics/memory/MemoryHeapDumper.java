package com.compomics.pladipus.core.control.runtime.diagnostics.memory;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;

public class MemoryHeapDumper {

    /**
     * The bean address
     */
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

    /**
     * The bean to get the diagnostics from
     */
    private static volatile HotSpotDiagnosticMXBean hotspotMBean;

    /**
     * Creates a dump file of the current memory, used for diagnostics
     *
     * @param dumpFile the file to write the memory dump to
     * @param live boolean indicating if the dump is live
     */
    public static void dumpHeap(File dumpFile, boolean live) {
        // initialize hotspot diagnostic MBean
        initHotspotMBean();
        try {
            hotspotMBean.dumpHeap(dumpFile.getAbsolutePath(), live);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    // initialize the hotspot diagnostic MBean field
    private static void initHotspotMBean() {
        if (hotspotMBean == null) {
            synchronized (MemoryHeapDumper.class) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }

    // get the hotspot diagnostic MBean from the
    // platform MBean server
    private static HotSpotDiagnosticMXBean getHotspotMBean() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean bean
                    = ManagementFactory.newPlatformMXBeanProxy(server,
                            HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
            return bean;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

}
