/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing.standard.maintenance;

import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 *
 * @author Kenneth Verheggen
 */
public class RebootStep extends ProcessingStep {

    @Override
    public boolean doAction() throws Exception {
        restartApplication();
        return true;
    }

    @Override
    public String getDescription() {
        return "A STEP TO TEST THE PLADIPUS INFRASTRUCTURE";
    }

    /**
     * Attempts to restart pladipus
     *
     * @throws IOException
     */
    public void restartApplication() throws URISyntaxException, IOException {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(RebootStep.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        /* is it a jar file? */
        if (!currentJar.getName().endsWith(".jar")) {
            return;
        }

        /* Build command: java -jar application.jar */
        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        Process start = builder.start();
        System.exit(0);
    }

}
