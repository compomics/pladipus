/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.controller.setup;

import static com.compomics.software.autoupdater.DownloadLatestZipFromRepo.downloadLatestZipFromRepo;
import com.compomics.software.autoupdater.GUIFileDAO;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JFrame;
import javax.xml.stream.XMLStreamException;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class InstallExample {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(InstallExample.class);
    /**
     * The progressdialog for searchGUI
     */
    private ProgressDialogX searchGUIDialog;
    /**
     * The progressdialog for peptideShaker
     */
    private ProgressDialogX peptideShakerDialog;
    /**
     * The progressdialog for deNovoGUI
     */
    private ProgressDialogX deNovoGUIDialog;

    public InstallExample() {

    }

    /**
     * Downloads and installs SearchGUI, DeNovoGUI and PeptideShaker to the local folders
     */
    public void install() {
        installSearchGUI();
        installPeptideShaker();
        installDeNovoGUI();
    }

    private void installSearchGUI() {
        File installFolder = new File(System.getProperty("user.home") + "/pladipus/tools/SearchGUI");
        installFolder.mkdir();
        searchGUIDialog = new ProgressDialogX(new JFrame(),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")),
                true);

        new Thread(() -> {
            try {

                searchGUIDialog.setPrimaryProgressCounterIndeterminate(true);
                searchGUIDialog.setTitle("Downloading SearchGUI. Please Wait...");
                searchGUIDialog.setVisible(true);
            } catch (IndexOutOfBoundsException e) {
                // ignore
            }
        }, "ProgressDialog").start();

        Thread thread = new Thread("DownloadThread") {
            @Override
            public void run() {
                try {
                    URL jarRepository = new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString());
                    downloadLatestZipFromRepo(installFolder, "SearchGUI", "eu.isas.searchgui", "SearchGUI", "searchgui.ico",
                            null, jarRepository, false, true, new GUIFileDAO(), searchGUIDialog);
                } catch (IOException | URISyntaxException | XMLStreamException e) {
                    LOGGER.error(e);
                } finally {
                    searchGUIDialog.setRunFinished();
                    searchGUIDialog.setVisible(false);
                }
            }
        };
        thread.start();
    }

    private void installPeptideShaker() {
        File installFolder = new File(System.getProperty("user.home") + "/pladipus/tools/PeptideShaker");
        installFolder.mkdir();
        peptideShakerDialog = new ProgressDialogX(new JFrame(),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker-orange.gif")),
                true);
        peptideShakerDialog.setLocationRelativeTo(searchGUIDialog);
        Point searchGUIDialogLocation = searchGUIDialog.getLocation();
        searchGUIDialogLocation.move((int) searchGUIDialogLocation.getX(), (int) searchGUIDialogLocation.getY() + searchGUIDialog.getHeight());
        peptideShakerDialog.setLocation(searchGUIDialogLocation);
        new Thread(new Runnable() {

            public void run() {
                try {
                    peptideShakerDialog.setPrimaryProgressCounterIndeterminate(true);
                    peptideShakerDialog.setTitle("Downloading PeptideShaker. Please Wait...");
                    peptideShakerDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                } finally {
                    peptideShakerDialog.setRunFinished();
                    peptideShakerDialog.setVisible(false);
                }
            }
        }, "ProgressDialog").start();

        Thread thread = new Thread("DownloadThread") {
            @Override
            public void run() {
                try {
                    URL jarRepository = new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString());
                    downloadLatestZipFromRepo(installFolder, "PeptideShaker", "eu.isas.peptideshaker", "PeptideShaker", "peptide-shaker.ico",
                            null, jarRepository, false, true, new GUIFileDAO(), peptideShakerDialog);
                } catch (IOException | URISyntaxException | XMLStreamException e) {
                    LOGGER.error(e);
                    e.printStackTrace();
                } finally {
                    peptideShakerDialog.setRunFinished();
                    peptideShakerDialog.setVisible(false);
                }
            }
        };
        thread.start();
    }

    private void installDeNovoGUI() {
        File installFolder = new File(System.getProperty("user.home") + "/pladipus/tools/DeNovoGUI");
        installFolder.mkdir();
        deNovoGUIDialog = new ProgressDialogX(new JFrame(),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker.gif")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/peptide-shaker-orange.gif")),
                true);
        deNovoGUIDialog.setLocationRelativeTo(peptideShakerDialog);
        Point searchGUIDialogLocation = peptideShakerDialog.getLocation();
        searchGUIDialogLocation.move((int) searchGUIDialogLocation.getX(), (int) searchGUIDialogLocation.getY() + peptideShakerDialog.getHeight());
        deNovoGUIDialog.setLocation(searchGUIDialogLocation);
        new Thread(new Runnable() {

            public void run() {
                try {
                    deNovoGUIDialog.setPrimaryProgressCounterIndeterminate(true);
                    deNovoGUIDialog.setTitle("Downloading DeNovoGUI. Please Wait...");
                    deNovoGUIDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                } finally {
                    deNovoGUIDialog.setRunFinished();
                    deNovoGUIDialog.setVisible(false);
                }
            }
        }, "ProgressDialog").start();

        Thread thread = new Thread("DownloadThread") {
            @Override
            public void run() {
                try {
                    URL jarRepository = new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString());
                    downloadLatestZipFromRepo(installFolder, "DeNovoGUI", "com.compomics.denovogui", "DeNovoGUI", "denovogui.ico",
                            null, jarRepository, false, true, new GUIFileDAO(), deNovoGUIDialog);
                } catch (IOException | URISyntaxException | XMLStreamException e) {
                    LOGGER.error(e);
                    e.printStackTrace();
                } finally {
                    deNovoGUIDialog.setRunFinished();
                    deNovoGUIDialog.setVisible(false);
                }
            }
        };
        thread.start();
    }

}
