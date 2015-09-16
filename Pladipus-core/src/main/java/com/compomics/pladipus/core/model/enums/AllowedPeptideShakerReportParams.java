package com.compomics.pladipus.core.model.enums;

/**
 * Enum class specifying the Command Line Parameters for PeptideShaker.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum AllowedPeptideShakerReportParams {

    CPS_FILE("in", "PeptideShaker project (.cpsx or .zip file)", true),
    EXPORT_FOLDER("out_reports", "Output folder for report files. (Existing files will be overwritten.)", true),
    REPORT_TYPE("reports", "Comma separated list of types of report to export. ", false),
    DOCUMENTATION_TYPE("documentation", "Comma separated list of types of report documentation to export. ", false);

    /**
     * Short Id for the CLI parameter.
     */
    public String id;
    /**
     * Explanation for the CLI parameter.
     */
    public String description;
    /**
     * Boolean indicating whether the parameter is mandatory.
     */
    public boolean mandatory;

    /**
     * Private constructor managing the various variables for the enum
     * instances.
     *
     * @param id the id
     * @param description the description
     * @param mandatory is the parameter mandatory
     */
    private AllowedPeptideShakerReportParams(String id, String description, boolean mandatory) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMandatory() {
        return mandatory;
    }

}
