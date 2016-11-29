/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.controller.setup;

import com.compomics.pladipus.core.control.distribution.service.UserService;
import com.compomics.pladipus.core.control.updates.ProcessingBeanUpdater;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.properties.NetworkProperties;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.jimmc.jshortcut.JShellLink;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author Kenneth Verheggen
 */
public class InstallPladipus {


    //TODO remove all this hardcoding

    /**
     * The current version of pladipus (//TODO get this from the POM or a
     * properties file???)
     */
    private static final String version = "1.2.1";
    /**
     * The available pladipus steps that need to be installed
     */
    private static final String[] modulesToInstall = new String[]{"search", "blast", "denovo", "msconvert","moff"};
    /**
     * The pladipus folder (in user home)
     */
    private static final File pladipusFolder = new File(System.getProperty("user.home") + "/pladipus/");
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

    private void setClassPath() throws IOException {
        NetworkProperties properties = NetworkProperties.getInstance();
        properties.setProperty("app.classpath", System.getProperty("user.home") + "/pladipus/external");
        properties.save();
    }

    /**
     * Installs a pladipus worker
     *
     * @throws IOException
     */
    public boolean installWorker() throws IOException, SecurityException {
        if (validateUser()) {
            setupPladipus();
            installPladipusModules();
            installProcessingBeanProperties();
            setClassPath();
            return true;
        } else {
            return false;
        }
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
        createDesktopIcon("Pladipus-" + version);
        setClassPath();
    }

    private void createDesktopIcon(String linkName) {
        //copy the ico file to the installation folder
        File icoFile = new File(pladipusFolder, "pladipus.ico");
        try (OutputStream out = new FileOutputStream(icoFile); InputStream in = getClass().getClassLoader().getResourceAsStream("images/pladipus.ico")) {
            if (in != null) {
                IOUtils.copy(in, out);
                //set this icon as the ico
                String iconFileLocation = icoFile.getAbsolutePath();
                JShellLink link = new JShellLink();
                link.setFolder(JShellLink.getDirectory("desktop"));
                link.setName(linkName);
                link.setIconLocation(iconFileLocation);
                link.setPath(jarFilePath);
                link.save();
            }
        } catch (IOException e) {

            LOGGER.error(e);
            System.out.println("An error occurred when trying to create a desktop shortcut...");
        }
    }

    /**
     * Writes an executable script to the desktop to start a worker
     *
     * @throws IOException
     */
    private void writeWorkerBash(String user, String password) throws IOException {
        String os = System.getProperty("os.name");
        long maxMemory = Runtime.getRuntime().maxMemory();
        int memory = (int) (0.9 * ((double) maxMemory / (double) (1024 * 1024)));
        String command = "java -Xmx" + memory + "m -Xms" + memory + "m -jar " + pladipusFolder + "/" + "Pladipus-execution-" + version + "/Pladipus-execution-" + version + ".jar -auto_pull -u " + user + " -p " + password;
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

    /**
     * Queries the user for the valid user login and password
     *
     * @throws IOException
     */
    public boolean validateUser() throws IOException {

        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
        label.add(new JLabel("Username", SwingConstants.RIGHT));
        label.add(new JLabel("Password", SwingConstants.RIGHT));
        panel.add(label, BorderLayout.WEST);

        JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
        JTextField username = new JTextField();
        controls.add(username);
        JPasswordField password = new JPasswordField();
        controls.add(password);

        panel.add(controls, BorderLayout.CENTER);

        int showConfirmDialog = JOptionPane.showConfirmDialog(null, panel, "Pladipus Login", JOptionPane.OK_CANCEL_OPTION);

        if (showConfirmDialog == JOptionPane.CANCEL_OPTION) {
            return false;
        }

        String user = username.getText();
        String pass = new String(password.getPassword());

        if (login(user, pass)) {
            writeWorkerBash(user, pass);
            return true;
        } else {
            throw new SecurityException("User credentials are incorrect!");
        }
    }

    private static boolean login(String user, String password) {
        boolean accept = false;
        UserService uService = UserService.getInstance();
        try {
            if (!uService.userExists(user)) {
                throw new SecurityException(user + " was not found !");
            } else if (uService.verifyUser(user, password)) {
                accept = true;
            } else {
                throw new SecurityException(user + " is not a authorized to push jobs !");
            }
        } catch (SQLException | UnsupportedEncodingException ex) {
            LOGGER.error(ex);
        }
        return accept;
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
