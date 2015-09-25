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
    private final ProgressDialogX progressDialog = new ProgressDialogX(true);

    public UserTableListSelectionListener(UserPanel panel) {
        this.panel = panel;
        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Retrieving Process Information. Please Wait...");
        progressDialog.setVisible(false);
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
        //Run_ID 
        try {
            JTable runTable = panel.getRunTable();
            //counter for multiple rows and hanging !
            int selected_row = runTable.getSelectedRow();
            int temp = Integer.parseInt(runTable.getValueAt(selected_row, 1).toString());
            if (temp != run_id) {
                if (selected_row > -1 && runTable.getSelectedRowCount() == 1) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                progressDialog.setVisible(true);
                            } catch (IndexOutOfBoundsException e) {
                                // ignore
                            }
                        }
                    }, "ProgressDialog").start();

                    new Thread("LaunchThread") {
                        @Override
                        public void run() {
                            try {
                                run_id = temp;
                                panel.setSelectedRunId(run_id);
                                panel.updateProcessTable();
                            } catch (Exception ex) {
                                LOGGER.error(ex);
                            } finally {
                                progressDialog.setRunFinished();
                                progressDialog.setVisible(false);
                                progressDialog.dispose();
                            }
                        }
                    }.start();
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }
}
