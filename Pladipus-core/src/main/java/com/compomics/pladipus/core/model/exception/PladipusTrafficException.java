package com.compomics.pladipus.core.model.exception;

import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Kenneth Verheggen
 */
public class PladipusTrafficException extends Exception {

    public PladipusTrafficException(String msg) {
        super(msg);
    }

    public PladipusTrafficException(Exception ex) {
        super(ex);
    }

}
