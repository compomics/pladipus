package com.compomics.pladipus.core.model.enums;

/**
 * Enum class specifying the Command Line Parameters for PeptideShaker.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum AllowedPeptideShakerFollowUpParams {

CPS_FILE("in", "PeptideShaker project (.cpsx or .zip file)", true),
    RECALIBRATION_FOLDER("recalibration_folder", "Output folder for the recalibrated files. (Existing files will be overwritten.)", false),
    RECALIBRATION_MODE("recalibration_mode", "Recalibration type. 0: precursor and fragment ions (default), 1: precursor only, 2: fragment ions only.", false),
    SPECTRUM_FOLDER("spectrum_folder", "Output folder for the spectra. (Existing files will be overwritten.)", false),
    PSM_TYPE("psm_type", "Type of PSMs. ", false),
    ACCESSIONS_FILE("accessions_file", "Output file to export the protein accessions in text format. (Existing files will be overwritten.)", false),
    ACCESSIONS_TYPE("accessions_type", "When exporting accessions, select a category of proteins. " , false),
    FASTA_FILE("fasta_file", "File where to export the protein details in fasta format. (Existing files will be overwritten.)", false),
    FASTA_TYPE("fasta_type", "When exporting protein details, select a category of proteins. ", false),
    PROGENESIS_FILE("progenesis_file", "Output file for identification results in Progenesis LC-MS compatible format. (Existing files will be overwritten.)", false),
    PROGENESIS_TYPE("progenesis_type", "Type of hits to export to Progenesis. ", false),
    PROGENESIS_TARGETED_PTMS("progenesis_ptms", "For the progenesis PTM export, the comma separated list of targeted PTMs in a list of PTM names", false),
    PEPNOVO_TRAINING_FOLDER("pepnovo_training_folder", "Output folder for PepNovo training files. (Existing files will be overwritten.)", false),
    PEPNOVO_TRAINING_RECALIBRATION("pepnovo_training_recalibration", "Indicate whether the exported mgf files shall be recalibrated. 0: No, 1: Yes (default).", false),
    PEPNOVO_TRAINING_FDR("pepnovo_training_fdr", "FDR used for the 'good spectra' export. If not set, the validation FDR will be used.", false),
    PEPNOVO_TRAINING_FNR("pepnovo_training_fnr", "FNR used for the 'bad spectra' export. If not set, the same value as for the 'good spectra' FDR will be used.", false),
    INCLUSION_LIST_FILE("inclusion_list_file", "Output file for an inclusion list of validated hits. (Existing files will be overwritten.)", false),
    INCLUSION_LIST_FORMAT("inclusion_list_format", "Format for the inclusion list. ", false),
    INCLUSION_LIST_PROTEIN_FILTERS("inclusion_list_protein_filters", "Protein inference filters to be used for the inclusion list export (comma separated). ", false),
    INCLUSION_LIST_PEPTIDE_FILTERS("inclusion_list_peptide_filters", "Peptide filters to be used for the inclusion list export (comma separated). ", false),
    INCLUSION_LIST_RT_WINDOW("inclusion_list_rt_window", "Retention time window for the inclusion list export (in seconds).", false);


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
    private AllowedPeptideShakerFollowUpParams(String id, String description, boolean mandatory) {
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
