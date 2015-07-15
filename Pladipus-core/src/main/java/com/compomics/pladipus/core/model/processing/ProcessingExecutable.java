package com.compomics.pladipus.core.model.processing;

import java.util.HashMap;

public abstract interface ProcessingExecutable {

    /**
     * Executes the executable
     *
     * @return a boolean to indicate if the process finished correctly
     * @throws Exception
     */
    public abstract boolean doAction()
            throws Exception;

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
