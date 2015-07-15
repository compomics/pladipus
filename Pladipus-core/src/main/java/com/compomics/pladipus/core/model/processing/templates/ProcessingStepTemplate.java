/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing.templates;

import com.compomics.pladipus.core.model.processing.ProcessingStep;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessingStepTemplate extends ProcessingStep {

    @Override
    public boolean doAction() {
        System.out.println("THIS IS A DUMMY PROCESSINGSTEP");
        return true;
    }

    @Override
    public String getDescription() {
        return "A dummy step";
    }

}
