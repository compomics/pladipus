/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.util;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.RunDAO;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.view.panels.impl.UserPanel;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.JOptionPane;

/**
 *
 * @author Kenneth
 */
public class RunUploader {

    /**
     * the progressDialog
     */
    private static ProgressDialogX progressDialog;

    public static void executeUpload(PladipusProcessingTemplate processingTemplate, File config, UserPanel userPanel) {
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
