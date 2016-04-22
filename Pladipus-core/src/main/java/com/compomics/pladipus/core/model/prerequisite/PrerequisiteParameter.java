package com.compomics.pladipus.core.model.prerequisite;

/**
 *
 * @author Kenneth Verheggen
 */
public enum PrerequisiteParameter {

    OS_WINDOWS("os.name", "windows", false),
    OS_LINUX("os.name", "linux", false),
    ARCH_32("os.arch", "32", false),
    ARCH_64("os.arch", "64", false),
    DISKSPACE("space", true),
    MEMORY("ram", true),
    CORES("cores", true);
    /**
     * The value for the option
     */
    private String optionValue;
    /**
     * boolean to check if the parameter can be edited
     */
    private final boolean editable;
    /**
     * The name of the system parameter
     */
    private final String systemParameter;

    private PrerequisiteParameter(String systemParameter, String optionValue, boolean editable) {
        this.optionValue = optionValue;
        this.editable = editable;
        this.systemParameter = systemParameter;
    }

    private PrerequisiteParameter(String systemParameter, boolean editable) {
        this.editable = editable;
        this.systemParameter = systemParameter;
    }

    private PrerequisiteParameter setOptionValue(String optionValue) {
        this.optionValue = optionValue;
        return this;
    }

    /**
     *
     * @return @returns the value for this parameter
     */
    public String getOptionValue() {
        return optionValue;
    }

    private boolean isEditable() {
        return editable;
    }

    /**
     *
     * @return the system name given for the parameter
     */
    public String getSystemParameterName() {
        return systemParameter;
    }

    /**
     * Gets a parameter with a custom value (for example : 1gb ram)
     *
     * @param parameter
     * @param value
     * @return the custom value set to this parameter
     * @throws UnsupportedOperationException
     */
    public PrerequisiteParameter getCustomOptionValue(PrerequisiteParameter parameter, String value) throws UnsupportedOperationException {
        if (parameter.isEditable()) {
            return parameter.setOptionValue(value);
        } else {
            throw new UnsupportedOperationException(parameter + " is not editable");
        }
    }

    /**
     *
     * @param other
     * @return a boolean whether this parameter equals another
     */
    public boolean equals(PrerequisiteParameter other) {
        return this.name().equalsIgnoreCase(other.name());
    }

}
