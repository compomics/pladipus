/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.database.dao;

/**
 *
 * @author Kenneth Verheggen
 */
public abstract class PladipusDAO {

    /**
     * A boolean indicating if the DAO was shut down
     */
    protected boolean shutdown;

    /**
     * Neatly closes the DAO
     */
    public void close() {
        shutdown = true;
    }

    /**
     *
     * @return whether the DAO's connection is already shutdown
     */
    public boolean isShutDown() {
        return shutdown;
    }

}
