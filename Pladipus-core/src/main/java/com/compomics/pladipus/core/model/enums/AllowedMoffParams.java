package com.compomics.pladipus.core.model.enums;

/**
 * Command line option parameters for Moff
 *
 * @author Andrea Argentini
 * @author Kenneth Verheggen
 */
public enum AllowedMoffParams {

    INPUT_FOLDER("-inputF", "the folder containing inputfiles", true),
    SAMPLE("-sample SAMPLE", "specify which replicate files are used for mbr [regular expr. are valid]", false),
    INPUT_EXTENSION("-ext", "specify the extension of the input file", false),
    LOG_FILE("-log_file_name", "a label for the log file", false),
    FILTER_WIDTH("-filt_width", "the iwidth value of the filter (k * mean(Dist_Malahobis) , k = 2 as default)", false),
    OUTLIER_FILTER("-out_filt", "filter outliers in each rt time allignment", false),
    WEIGHTS("-weight_comb", "weights for model combination (0 for no weight (default) 1 weighted devised by model errors)", false),
    TOLERANCE("-tol", "specify the tolerance in ppm", false),
    RT_WINDOW("-rt_w", "specify the rt window for xic", false),
    RT_P_WINDOW("-rt_p", "specify the rt window for peaks", false),
    RT_P_MATCH("-rt_p_match", "specify the time windows for a matched peptide peeak", false),
    RAW_REPO("-raw_repo", "specify the raw file repository", false),
    OUTPUT("-output_folder", "specify the output folder", false);
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
    private AllowedMoffParams(String id, String description, boolean mandatory) {
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
