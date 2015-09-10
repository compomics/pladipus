/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.controller.setup;

import com.compomics.pladipus.core.control.updates.ProcessingBeanUpdater;
import com.compomics.pladipus.core.control.util.ZipUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import net.jimmc.jshortcut.JShellLink;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class InstallPladipus {

    /**
     * The current version of pladipus (//TODO get this from the POM or a
     * properties file???)
     */
    private static final String version = "0.3.0";
    /**
     * The available pladipus steps that need to be installed
     */
    private static final String[] modulesToInstall = new String[]{"search", "blast", "denovo","msconvert"};
    /**
     * The pladipus folder (in user home)
     */
    private static final File pladipusFolder = new File(System.getProperty("user.home") + "/.compomics/pladipus/");
    /**
     * The path to the jar file post installing
     */
    private static final String jarFilePath = pladipusFolder.getAbsolutePath() + "/" + "Pladipus-execution-" + version + "/Pladipus-execution-" + version + ".jar";
    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(InstallPladipus.class);

    public InstallPladipus() {

    }

    /**
     * Installs a pladipus worker
     *
     * @throws IOException
     */
    public void installWorker() throws IOException {
        installPladipusModules();
        installProcessingBeanProperties();
        writeWorkerBash();
    }

    /**
     * Installs the pladipus GUI
     *
     * @throws IOException
     */
    public void installConsole() throws IOException {
        setupPladipus();
        installPladipusModules();
        installProcessingBeanProperties();
        createDesktopIcon();
    }

    private void createDesktopIcon() {
        //copy the ico file to the installation folder
        File icoFile = new File(pladipusFolder, "pladipus.ico");
        try (OutputStream out = new FileOutputStream(icoFile); InputStream in = getClass().getClassLoader().getResource("images/pladipus.ico").openStream()) {
            IOUtils.copy(in, out);
            //set this icon as the ico
            String iconFileLocation = icoFile.getAbsolutePath();
            JShellLink link = new JShellLink();
            link.setFolder(JShellLink.getDirectory("desktop"));
            link.setName("Pladipus-" + version);
            link.setIconLocation(iconFileLocation);
            link.setPath(jarFilePath);
            link.save();
        } catch (Exception e) {
            System.out.println("An error occurred when trying to create a desktop shortcut...");
            e.printStackTrace();
        }
    }

    /**
     * Writes an executable script to the desktop to start a worker
     *
     * @throws IOException
     */
    public void writeWorkerBash() throws IOException {
        String os = System.getProperty("os.name");
        long maxMemory = Runtime.getRuntime().maxMemory();
        int memory = (int) (0.9 * ((double) maxMemory / (double) (1024 * 1024)));
        String command = "java -Xmx" + memory + "m -Xms" + memory + "m -cp " + pladipusFolder + "/" + "Pladipus-execution-"+version+"/Pladipus-execution-"+version+".jar com.compomics.pladipus.playground.ConsumerStarter";
        FileWriter bashWriter;
        if (os.toLowerCase().contains("windows")) {
            File bashFile = new File(System.getProperty("user.home") + "/Desktop/Pladipus-Worker.bat");
            bashWriter = new FileWriter(bashFile);
        } else {
            File bashFile = new File(System.getProperty("user.home") + "/Desktop/Pladipus-Worker.sh");
            bashWriter = new FileWriter(bashFile);
        }
        bashWriter.append(command).flush();
        bashWriter.close();
    }

    private void installProcessingBeanProperties() {
        //get instance initializes the file if it's not there yet from resources
        ProcessingBeanUpdater.getInstance();
    }

    private void setupPladipus() throws IOException {
        //1. extract
        URL inputUrl = getClass().getResource("/Pladipus-execution-" + version + ".zip");
        File dest = new File(pladipusFolder, "Pladipus-execution-" + version + ".zip");
        dest.getParentFile().mkdirs();
        FileUtils.copyURLToFile(inputUrl, dest);
        //2. unzip
        ZipUtils.unzipArchive(dest, pladipusFolder);
        //3. cleanup
        dest.deleteOnExit();
    }

    private void installPladipusModules() throws IOException {
        //search
        for (String aModuleName : modulesToInstall) {
            LOGGER.debug("Installing " + aModuleName);
            URL inputUrl = getClass().getResource("/modules/Pladipus-" + aModuleName + "-" + version + ".jar");
            File dest = new File(pladipusFolder, "/external/Pladipus-" + aModuleName + "-" + version + ".jar");
            dest.getParentFile().mkdirs();
            FileUtils.copyURLToFile(inputUrl, dest);
        }
    }

}
