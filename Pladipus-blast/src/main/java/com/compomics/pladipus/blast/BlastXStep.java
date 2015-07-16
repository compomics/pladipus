/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.blast;

import java.io.File;

/**
 *
 * @author Kenneth Verheggen
 */
public class BlastXStep extends BlastPStep {

    public BlastXStep() {

    }

    @Override
    public File getBlastExecutable() {
        return new File(parameters.get("blastFolder"), "blastx");
    }

    @Override
    public String getDescription() {
        return "Translated blast";
    }

}
