/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.properties;

import java.io.File;
import java.util.Properties;

/**
 *
 * @author Kenneth Verheggen
 */
public abstract class PladipusProperties extends Properties {

    protected static File defaultPropFile;

    public File getPropertiesFile() {
        return this.defaultPropFile;
    }

    /**
     * redirect the properties from another location than the default
     *
     * @param propertiesFile
     */
    public void setPropertiesFile(File propertiesFile) {
        this.defaultPropFile = propertiesFile;
    }
}
