package com.compomics.pladipus.core.model.enums;

/**
 * Command line option parameters for SearchCLI.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public enum AllowedSearchGUIParams {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPORTANT: Any change here must be reported in the wiki: 
    // https://github.com/compomics/searchgui/wiki/SearchCLI.
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    SPECTRUM_FILES("spectrum_files", "Spectrum files (mgf format), comma separated list or an entire folder.", true),
    OUTPUT_FOLDER("output_folder", "The output folder.", true),
    IDENTIFICATION_PARAMETERS("id_params", "The search parameters file. Generated from the GUI or using IdentificationParametersCLI.", true),
    THREADS("threads", "Number of threads to use for the processing, default: the number of cores.", false),
    OMSSA("omssa", "Turn the OMSSA search on or off (1: on, 0: off, default is '1').", false),
    XTANDEM("xtandem", "Turn the X!Tandem search on or off (1: on, 0: off, default is '1').", false),
    MSGF("msgf", "Turn the MS-GF+ search on or off (1: on, 0: off, default is '1').", false),
    MS_AMANDA("ms_amanda", "Turn the MS Amanda search on or off (1: on, 0: off, default is '1').", false),
    MYRIMATCH("myrimatch", "Turn the MyriMatch search on or off (1: on, 0: off, default is '1').", false),
    COMET("comet", "Turn the Comet search on or off (1: on, 0: off, default is '1').", false),
    TIDE("tide", "Turn the Tide search on or off (1: on, 0: off, default is '1').", false),
    ANDROMEDA("andromeda", "Turn the Andromeda search on or off (1: on, 0: off, default is '1').", false),
    OMSSA_LOCATION("omssa_folder", "The folder where OMSSA is installed, defaults to the provided version for the given OS.", false),
    XTANDEM_LOCATION("xtandem_folder", "The folder where X!Tandem is installed, defaults to the provided version for the given OS.", false),
    MSGF_LOCATION("msgf_folder", "The folder where MS-GF+ is installed, defaults to the included version.", false),
    MS_AMANDA_LOCATION("ms_amanda_folder", "The folder where MS Amanda is installed, defaults to the included version.", false),
    MYRIMATCH_LOCATION("myrimatch_folder", "The folder where MyriMatch is installed, defaults to the included version.", false),
    COMET_LOCATION("comet_folder", "The folder where Comet is installed, defaults to the included version.", false),
    TIDE_LOCATION("tide_folder", "The folder where Tide is installed, defaults to the included version.", false),
    ANDROMEDA_LOCATION("andromeda_folder", "The folder where Andromeda is installed, defaults to the included version.", false),
    MGF_SPLITTING_LIMIT("mgf_splitting", "The maximum mgf file size in MB before splitting the mgf. Default is '1000'.", false),
    MGF_MAX_SPECTRA("mgf_spectrum_count", "The maximum number of spectra per mgf file when splitting. Default is '25000'.", false),
    DUPLICATE_TITLE_HANDLING("correct_titles", "Correct for duplicate spectrum titles. (0: no correction, 1: rename spectra, 2: delete spectra, default is '1').", false),
    MISSING_TITLE_HANDLING("missing_titles", "Add missing spectrum titles. (0: no correction, 1: add missing spectrum titles, default is '0').", false),
    SPECIES("species", "The species to use for the gene annotation. Supported species are listed in the GUI.", false),
    SPECIES_TYPE("species_type", "The species type to use for the gene annotation. Supported species types are listed in the GUI.", false),
    PROTEIN_INDEX("protein_index", "Turn the FASTA file indexing on/off. (1: on, 0: off, default is '0').", false),
    OUTPUT_OPTION("output_option", "Optional result file compression, default is '0'.", false),
    OUTPUT_DATA("output_data", "Include mgf and FASTA file in zipped output (0: no, 1: yes, default is '0').", false),
    OUTPUT_DATE("output_date", "Include date in output name (0: no, 1: yes, default is '0').", false),
    MAKEBLASTDB_LOCATION("makeblastdb_folder", "The folder where makeblastdb is installed, defaults to the provided version for the given OS.", false);

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
    private AllowedSearchGUIParams(String id, String description, boolean mandatory) {
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
