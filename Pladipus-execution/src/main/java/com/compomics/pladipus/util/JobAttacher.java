/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.util;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.distribution.service.RunService;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.RunDAO;
import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.view.panels.impl.UserPanel;
import com.compomics.pladipus.view.util.menu.RunPopupMenu;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth
 */
public class JobAttacher {
    private static ProgressDialogX progressDialog;

    public static void queryUserForJobs(UserPanel userPanel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Job configuration", "tsv"));
        int result = fileChooser.showOpenDialog(userPanel);
        if (result == JFileChooser.APPROVE_OPTION) {
            File jobConfigFile = fileChooser.getSelectedFile();
            System.out.println("Selected file: " + jobConfigFile.getAbsolutePath());
            int[] selectedRows = userPanel.getRunTable().getSelectedRows();
            if (selectedRows.length == 1) {
                try {
                    PladipusProcessingTemplate templateForRun = RunService.getInstance().getTemplateForRun(Integer.parseInt(String.valueOf(userPanel.getRunTable().getValueAt(selectedRows[0], 1))));
                    executeUpload(userPanel,templateForRun, jobConfigFile);
                } catch (SQLException | IOException | StepLoadingException | ParserConfigurationException | SAXException ex) {
                    Logger.getLogger(RunPopupMenu.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                JOptionPane.showMessageDialog(userPanel,
                        "Please select a single run to attach jobs to",
                        "Multiple selection of runs not allowed",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    
        private static void executeUpload(UserPanel userPanel,PladipusProcessingTemplate processingTemplate, File config) {
        progressDialog = new ProgressDialogX(true);
        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Adding jobs to run. Please Wait...");

        new Thread(new Runnable() {
            public void run() {
                try {
                    progressDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();

        new Thread("SaveThread") {
            @Override
            public void run() {
                try {
                    XMLTemplateInterpreter xmlInterpreter = XMLTemplateInterpreter.getInstance();
                    //store in the database
                    LinkedList<HashMap<String, String>> readLocalProcessingParameters = xmlInterpreter.readLocalProcessingParameters(processingTemplate, config);
                    RunDAO rInstance = RunDAO.getInstance();
                    int runID = rInstance.createRun(processingTemplate);
                    rInstance.addToRun(runID, readLocalProcessingParameters);
                    progressDialog.setRunFinished();
                    try {
                        userPanel.updateRunTable();
                    } catch (Exception ex) {
                        progressDialog.setRunFinished();
                            JOptionPane.showMessageDialog(userPanel,
                                ex.getMessage(),
                                "Run Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    try {
                        userPanel.updateProcessTable();
                    } catch (Exception ex) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(userPanel,
                                ex.getMessage(),
                                "Run Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    progressDialog.setRunFinished();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Could not create run" + System.lineSeparator() + e,
                            "Run Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.start();
    }
    
}
