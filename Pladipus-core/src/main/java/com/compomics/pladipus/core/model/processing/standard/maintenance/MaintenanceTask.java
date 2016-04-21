package com.compomics.pladipus.core.model.processing.standard.maintenance;

/**
 *
 * @author Kenneth Verheggen
 */
public enum MaintenanceTask {

    SHUTDOWN("Shutdown", "com.compomics.pladipus.core.model.processing.standard.maintenance.ShutdownStep"),
    REBOOT("Reboot", "com.compomics.pladipus.core.model.processing.standard.maintenance.RebootStep"),
    UPDATE_PROCESSBEANS("Update Processing Steps", "com.compomics.pladipus.core.model.processing.standard.maintenance.UpdateProcessingBeansStep");
    /**
     * The maintenance task name
     */
    private final String shortName;
    /**
     * The maintenance task fully defined class name
     */
    private final String className;

    private MaintenanceTask(String shortName, String className) {
        this.shortName = shortName;
        this.className = className;
    }

    /**
     *
     * @return the classes short name. This is equivalent to the "toString"
     * method
     */
    public String getShortName() {
        return shortName;
    }

    /**
     *
     * @return the fully defined class name
     */
    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return shortName;
    }

}
