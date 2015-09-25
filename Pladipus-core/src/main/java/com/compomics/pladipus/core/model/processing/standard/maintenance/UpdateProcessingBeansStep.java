/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing.standard.maintenance;

import com.compomics.pladipus.core.control.updates.ProcessingBeanUpdater;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class UpdateProcessingBeansStep extends ProcessingStep {
     /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(UpdateProcessingBeansStep.class);
    
    @Override
    public boolean doAction() throws Exception {
        String className = getParameters().get("class");
        LOGGER.debug("Updating processing bean configuration...");
        ProcessingBeanUpdater instance = ProcessingBeanUpdater.getInstance();
        instance.addNewProcessingStep(className);
        return true;
    }
    
    @Override
    public String getDescription() {
        return "Loading a new Pladipus Step onto the platform";
    }
}
