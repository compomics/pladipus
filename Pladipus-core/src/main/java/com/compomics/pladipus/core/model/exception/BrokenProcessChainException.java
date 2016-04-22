package com.compomics.pladipus.core.model.exception;

import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @author Kenneth Verheggen
 */
public class BrokenProcessChainException extends Exception {

    public BrokenProcessChainException(String msg) {
        super(msg);
    }

}
