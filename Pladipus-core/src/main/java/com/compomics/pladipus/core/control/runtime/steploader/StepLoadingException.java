/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.runtime.steploader;

/**
 *
 * @author Kenneth Verheggen
 */
public class StepLoadingException extends Exception {

    /**
     * This is a wrapper for all errors that can occurr when dynamically
     * updating the classpath
     *
     * @param e the original exception
     */
    public StepLoadingException(Exception e) {
        super(e);
    }
        /**
     * This is a wrapper for all errors that can occurr when dynamically
     * updating the classpath
     *
     * @param e the original exception
     */
    public StepLoadingException(String e) {
        super(e);
    }
}
