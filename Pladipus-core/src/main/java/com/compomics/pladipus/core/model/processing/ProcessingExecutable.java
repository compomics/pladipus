package com.compomics.pladipus.core.model.processing;

import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import java.util.HashMap;

public abstract interface ProcessingExecutable {

    /**
     * Executes the executable
     *
     * @return a boolean to indicate if the process finished correctly
     * @throws com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException
     * @throws com.compomics.pladipus.core.model.exception.PladipusProcessingException
     */
    public abstract boolean doAction()
            throws UnspecifiedPladipusException,PladipusProcessingException;

    /**
     *
     * @return the description of the executable
     */
    public abstract String getDescription();

    /**
     *
     * @return the parameters of the executable
     */
    public abstract HashMap<String, String> getParameters();
}
