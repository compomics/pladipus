package com.compomics.pladipus.core.model.exception;

import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessStepInitialisationException extends Exception {

    public ProcessStepInitialisationException(String msg) {
        super(msg);
    }

}
