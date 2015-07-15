/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.view.panels;

/**
 *
 * @author Kenneth Verheggen
 */
public interface UpdatingPanel {

    /**
     * Start the updater
     */
    public void activate();

    /**
     * Stop the updater
     */
    public void deactivate();
}
