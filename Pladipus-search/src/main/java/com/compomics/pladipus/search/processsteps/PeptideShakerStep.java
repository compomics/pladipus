/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.search.processbuilder.PeptideShakerProcess;
import java.io.File;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerStep extends ProcessingStep {

    public PeptideShakerStep() {

    }

    @Override
    public boolean doAction() throws Exception, Exception {
        System.out.println("Running " + this.getClass().getName());

        System.out.println("Closing and removing all current connections...");

        File peptideShakerJar = getJar();

        File temp = new File(parameters.get("temp"));
        File parameterFile = new File(parameters.get("tempParameterFile"));
        PeptideShakerProcess process = new PeptideShakerProcess(parameters.get("assay"), temp, parameterFile, peptideShakerJar);
        parameters.put("cps", process.getResultingCpsFile().getAbsolutePath());
        //TODO REPLACE THIS WITH THE ACTUAL OUTPUTFOLDER OR WAIT TILL THE VERY END IN THE CLEANING STEP?
        process.setOutputFolder(temp);
        //place the modified temp usermod in the searchgui config folder...keep a backup as classpathresource
        //before running...do a garbage collection to get rid of the derby connection?

        ProcessingEngine.startProcess(peptideShakerJar, process.generateCommand());
        //run searchgui with the existing files

        return true;
    }

    public File getJar() {
        //check if this is possible in another way...
        File peptideShakerFolder = new File(parameters.get("PeptideShaker"));
        File jarParent = peptideShakerFolder.listFiles()[0];
        String version = jarParent.getName();
        return new File(jarParent, version + ".jar");
    }

    @Override
    public String getDescription() {
        return "Running PeptideShaker";
    }
}
