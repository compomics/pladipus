/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.prerequisite;

/**
 *
 * @author Kenneth Verheggen
 */
public enum PrerequisiteType {
    CORES("The amount of cores"),
    OS("The operating system"),
    ARCHITECTURE("The architecture of the system (32bit vs 64bit)"),
    MEMORY("The amount of RAM the machine has"),
    DEDICATED("The only type of job this machine can run");
    private final String description;
    private String[] options;

    private PrerequisiteType(String description, String[] options) {
        this.description = description;
        this.options = options;
    }

    private PrerequisiteType(String description) {
        this.description = description;
        this.options = new String[0];
    }

    public String getDescription() {
        return description;
    }
}
