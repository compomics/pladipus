/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing.templates;

import java.util.Objects;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessingParameterTemplate {

    /**
     * boolean indicating if this parameter can be defaulted
     */
    private boolean mandatory = false;
    /**
     * The name of the parameter
     */
    private final String name;
    /**
     * The value of the parameter
     */
    private String value;

    public ProcessingParameterTemplate(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ProcessingParameterTemplate(String name) {
        this.name = name;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory() {
        this.mandatory = true;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     *
     * @return whether the parameter is filled in
     */
    public boolean isValid() {
        //TODO Check for correct input values?
        return mandatory && (value != null && !value.isEmpty());
    }

    @Override
    public String toString() {
        return name + "\t" + value + "\t" + mandatory + "\t" + isValid();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.mandatory ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.name);
        hash = 71 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProcessingParameterTemplate other = (ProcessingParameterTemplate) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}
