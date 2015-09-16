package com.compomics.pladipus.core.model.enums;

/**
 * Enum class specifying the Command Line Parameters for PeptideShaker.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum AllowedPeptideShakerParams {

    ///////////////////////////////////////////////////////////////////////////
    // IMPORTANT: Any change here must be reported in the wiki: 
    // http://compomics.github.io/peptide-shaker/wiki/peptideshakercli.html
    ///////////////////////////////////////////////////////////////////////////
    EXPERIMENT("experiment", "Specifies the experiment name.", true),
    SAMPLE("sample", "Specifies the sample name.", true),
    REPLICATE("replicate", "The replicate number.", true),
    SPECTRUM_FILES("spectrum_files", "Spectrum files (mgf format), comma separated list or an entire folder.", false),
    IDENTIFICATION_FILES("identification_files", "Identification files (.t.xml, .mzid, .cvs, .omx, .dat, .txt, .pep.xml, .zip), comma separated list or an entire folder.", true),
    PEPTIDESHAKER_OUTPUT("out", "PeptideShaker output file. Note: if file exists it will be overwritten.", true),
    PSM_FDR("psm_FDR", "FDR at the PSM level (default 1% FDR: '1').", false),
    PEPTIDE_FDR("peptide_FDR", "FDR at the peptide level (default 1% FDR: '1').", false),
    PROTEIN_FDR("protein_FDR", "FDR at the protein level (default 1% FDR: '1').", false),
    IDENTIFICATION_PARAMETERS("id_params", "The identification parameters file. Generated using SeachGUI or via IdentificationParametersCLI.", false),
    GUI("gui", "Use a dialog to display the progress (1: true, 0: false, default is '0').", false),
    PTM_SCORE("ptm_score", "The PTM probabilistic score to use for PTM localization. If no score is given, no probabilistic score will be used.", false),
    PTM_THRESHOLD("ptm_threshold", "The threshold to use for the PTM scores. Automatic mode will be used if not set.", false),
    SCORE_NEUTRAL_LOSSES("score_neutral_losses", "Include neutral losses in spectrum annotation of the PTM score (1: true, 0: false, default is '0').", false),
    PROTEIN_FRACTION_MW_CONFIDENCE("protein_fraction_mw_confidence", "Minimum confidence required for a protein in the fraction MW plot (default 95%: '95.0').", false),
    MIN_PEPTIDE_LENGTH("min_peptide_length", "Minimim peptide length filter (default is '4').", false),
    MAX_PEPTIDE_LENGTH("max_peptide_length", "Maximum peptide length filter (default is '30').", false),
    MAX_PRECURSOR_ERROR("max_precursor_error", "Maximum precursor error filter (no filter is used by default). See also max_precursor_error_type.", false),
    MAX_PRECURSOR_ERROR_TYPE("max_precursor_error_type", "Maximum precursor error type (0: ppm, 1: Da, default is '0'). See also max_precursor_error.", false),
    EXCLUDE_UNKNOWN_PTMS("exclude_unknown_ptms", "Exclude unknown PTMs (1: true, 0: false, default is '1').", false),
    SPECIES("species", "The species to use for the gene annotation. Supported species are listed in the GUI.", false),
    SPECIES_TYPE("species_type", "The species type to use for the gene annotation. Supported species types are listed in the GUI.", false),
    SPECIES_UPDATE("species_update", "Check for new species information in Ensembl and update if possible. (1: true, 0: false, default is '0').", false),
    ZIP("zip", "Exports the entire project as a zip file in the file specified.", false),
    THREADS("threads", "The number of threads to use. Defaults to the number of available CPUs.", false);

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
    private AllowedPeptideShakerParams(String id, String description, boolean mandatory) {
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
