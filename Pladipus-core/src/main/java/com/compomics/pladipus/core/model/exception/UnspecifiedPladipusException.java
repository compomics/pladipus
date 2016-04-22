package com.compomics.pladipus.core.model.exception;

/**
 *
 * @author Kenneth Verheggen
 */
public class UnspecifiedPladipusException extends Exception {

    public UnspecifiedPladipusException(String msg) {
        super(msg);
    }

    public UnspecifiedPladipusException(Exception e) {
        super(e);
    }

    public UnspecifiedPladipusException(Throwable e) {
        super(e);
    }

    public Throwable getOriginalCause() {
        return getCause().getCause();
    }

}
