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
public enum AllowedBLASTParams {

    BLAST_FOLDER("blast_folder", "The location of the bin folder for the BLAST algorithms upon install", true),
    DBTYPE("dbtype", "The type of the blast input. Can be nucl or prot'", true),
    BLAST_TYPE("blastType","The type of blast that will be executed, can be blastX,blastP or blastN",false),
    QUERY("query", "A fasta file with the sequences that need to be queried", true),
    DATABASE("db", "A fasta sequence database", true),
    OUTPUT("out", "The outputfolder for the results", true);

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
    private AllowedBLASTParams(String id, String description, boolean mandatory) {
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
