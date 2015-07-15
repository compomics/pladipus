/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.compomics.pladipus.core.control.runtime.steploader;

import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.net.MalformedURLException;

/**
 *
 * @author Kenneth Verheggen
 */
public interface StepLoader {

    /**
     * This method loads the class , starting from the folder specified in the
     * constructor
     *
     * @param className the name of the class to be loaded
     * @return the processingstep representation of the given class
     * @throws MalformedURLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws jInvocationTargetException
     * @throws ClassNotFoundException
     */
    ProcessingStep loadProcessingStep(String className) throws Exception;
    
}
