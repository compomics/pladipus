/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.enums;

/**
 *
 * @author Kenneth
 */
public enum AllowedDenovoGUIParams {
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPORTANT: Any change here must be reported in the wiki: 
    // https://github.com/compomics/denovogui/wiki/DeNovoCLI.
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    SPECTRUM_FILES("spectrum_files", "Spectrum files (mgf format), comma separated list or an entire folder.", true),
    OUTPUT_FOLDER("output_folder", "The output folder.", true),
    IDENTIFICATION_PARAMETERS("id_params", "A search parameters file. Can be generated from the GUI or using the IdentificationParametersCLI (see http://compomics.github.io/compomics-utilities/wiki/identificationparameterscli.html for details).", false),
    THREADS("threads", "The number of threads to use for the processing. Default is the number of cores available.", false),
    PEPNOVO("pepnovo", "Turn the PepNovo+ sequencing on or off (1: on, 0: off, default is '1'). (At least one sequencing method has to be enabled.)", false),
    DIRECT_TAG("directag", "Turn the DirecTag sequencing on or off (1: on, 0: off, default is '1'). (At least one sequencing method has to be enabled.)", false),
    PNOVO("pnovo", "Turn the pNovo+ sequencing on or off (1: on, 0: off, default is '0'). (At least one sequencing method has to be enabled.)", false),
    NOVOR("novor", "Turn the Novor sequencing on or off (1: on, 0: off, default is '0'). (At least one sequencing method has to be enabled.)", false),
    PEPNOVO_LOCATION("pepnovo_folder", "The PepNovo+ executable, defaults to the OS dependent versions included with DeNovoGUI.", false),
    DIRECTAG_LOCATION("directag_folder", "The DirecTag executable, defaults to the OS dependent versions included with DeNovoGUI.", false),
    PNOVO_LOCATION("pnovo_folder", "The pNovo+ executable, defaults to the OS dependent versions included with DeNovoGUI.", false),
    NOVOR_LOCATION("novor_folder", "The Novor executable, defaults to the OS dependent versions included with DeNovoGUI.", false);

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
    private AllowedDenovoGUIParams(String id, String description, boolean mandatory) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    
 
}