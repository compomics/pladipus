package com.compomics.pladipus.core.model.exception;

import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Kenneth Verheggen
 */
public class PladipusProcessingException extends Exception {

    public PladipusProcessingException(String msg) {
        super(msg);
    }

    public PladipusProcessingException(Exception e) {
        super(e);
    }

}
