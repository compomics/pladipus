package com.compomics.pladipus.core.model.exception;

/**
 *
 * @author Kenneth Verheggen
 */
public class XMLInterpreterException extends Exception {

    public XMLInterpreterException(String msg) {
        super(msg);
    }

    public XMLInterpreterException(java.lang.Exception ex) {
          super(ex);
    }

}
