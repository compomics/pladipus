/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.view.util.listener;

import com.compomics.pladipus.view.panels.impl.UserPanel;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class UserTableListSelectionListener implements ListSelectionListener {

    private final UserPanel panel;
    private final Logger LOGGER = Logger.getLogger(UserTableListSelectionListener.class);
    private int run_id;
    private ProgressDialogX progressDialog;

    public UserTableListSelectionListener(UserPanel panel) {
        this.panel = panel;
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
        //Run_ID 
        try {
            JTable runTable = panel.getRunTable();
            //counter for multiple rows and hanging !

            int selected_row = runTable.getSelectedRow();
            if (selected_row > -1 && runTable.getSelectedRowCount() == 1) {
                run_id = Integer.parseInt(runTable.getValueAt(selected_row, 1).toString());
                //do this in a swing worker?
                doUpdate();
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    public void doUpdate() {

        progressDialog = new ProgressDialogX(true);
        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Retrieving Process Information. Please Wait...");

        new Thread(new Runnable() {
            public void run() {
                try {
                    progressDialog.setVisible(true);
                } catch (Exception e) {
                    LOGGER.error(e);
                } finally {
                    progressDialog.setRunFinished();
                }
            }
        }, "ProgressDialog").start();

        new Thread("SaveThread") {
            @Override
            public void run() {
                try {
                    if (run_id > 0) {
                        panel.setSelectedRunId(run_id);
                        panel.updateProcessTable();
                    }
                } catch (Exception e) {
                    LOGGER.error(e);
                } finally {
                    progressDialog.setRunFinished();

                }
            }
        }.start();
    }
}
