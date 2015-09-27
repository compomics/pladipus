/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.view.util.menu;

import com.compomics.pladipus.core.control.distribution.service.ProcessService;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ProcessDAO;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.DeleteOperation;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.compomics.pladipus.view.dialogs.run.ProcessDetailDialog;
import com.compomics.pladipus.view.panels.impl.UserPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessPopupMenu extends JPopupMenu {

    /**
     * The parenting user panel
     */
    private final UserPanel userPanel;
    /**
     * The table including the progress bars
     */
    private final JTable processTable;

    public ProcessPopupMenu(UserPanel userPanel) {
        super();
        this.userPanel = userPanel;
        this.processTable = userPanel.getProcessTable();
        addStartItem();
        addCancelAction();
        addModifyItem();
        addDeleteAction();
    }

    private void addCancelAction() {
        JMenuItem deleteItem = new JMenuItem("Cancel");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = processTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    int dialogResult = JOptionPane.showConfirmDialog(ProcessPopupMenu.this, "Are you sure you want to stop the selected process(es)?" 
                            + System.lineSeparator() + "Stopping a Job will lose all current progress...");
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        try {
                            ProcessDAO pDAO = ProcessDAO.getInstance();
                            List<Integer> processesToCancel = new ArrayList<Integer>();
                            for (int selectedRow : selectedRows) {
                                int processID = Integer.parseInt(String.valueOf(processTable.getValueAt(selectedRow, 1)));
                                DeleteOperation dO = new DeleteOperation();
                                dO.deleteJobFromQueue(CompomicsQueue.JOB, processID);
                                pDAO.setQueued(processID, false);
                            }
                            pDAO.resetProcesses(processesToCancel);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(ProcessPopupMenu.this,
                                    "Could not cancel the selection: " + ex,
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                userPanel.activate();
            }
        });
        add(deleteItem);
    }

    private void addModifyItem() {
        JMenuItem modifyItem = new JMenuItem("Modify");
        modifyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = processTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    List<Integer> selectedProcesses = new ArrayList<>();
                    for (int selectedRow : selectedRows) {
                        selectedProcesses.add(Integer.parseInt(String.valueOf(processTable.getValueAt(selectedRow, 1))));
                    }
                    try {
                        ProcessDetailDialog dialog = new ProcessDetailDialog(null, true, selectedProcesses.get(0));
                        dialog.setLocationRelativeTo(null);
                        dialog.setVisible(true);
                        userPanel.activate();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ProcessPopupMenu.this,
                                "Could not modify the selection: " + ex,
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        add(modifyItem);
    }

    private void addStartItem() {
        JMenuItem starItem = new JMenuItem("Start");
        starItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = processTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    if (selectedRows.length == 1) {

                    }
                    try {
                        ProcessDAO pDAO = ProcessDAO.getInstance();
                        PladipusProcessingTemplate runTemplate = null;
                        for (int selectedRow : selectedRows) {
                            int processID = Integer.parseInt(String.valueOf(processTable.getValueAt(selectedRow, 1)));
                            if (runTemplate == null) {
                                runTemplate = pDAO.getTemplate(processID);
                            }
                            //is the process complete?
                            if (!pDAO.isCompletedProcess(processID) && !pDAO.isQueued(processID)) {
                                try (CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.JOB, pDAO.getXMLForProcess(processID), processID)) {
                                    producer.run();
                                }
                                pDAO.setQueued(processID, true);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "Could not start the task(s): " + System.lineSeparator() + ex,
                                "Task Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        add(starItem);
    }

    private void addDeleteAction() {
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = processTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    int dialogResult = JOptionPane.showConfirmDialog(ProcessPopupMenu.this, "Are you sure you want to delete the selected process(es)?");
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        List<Integer> processesToDelete = new ArrayList<>();
                        for (int selectedRow : selectedRows) {
                            processesToDelete.add(Integer.parseInt(String.valueOf(processTable.getValueAt(selectedRow, 1))));
                        }
                        try {
                            ProcessService.getInstance().removeProcesses(processesToDelete);
                            userPanel.activate();
                            DefaultTableModel model = (DefaultTableModel) processTable.getModel();

                            for (int i = selectedRows.length - 1; i > 0; i--) {
                                model.removeRow(selectedRows[i]);
                            }

                            processTable.setModel(model);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(ProcessPopupMenu.this,
                                    "Could not delete the selection: " + ex,
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        add(deleteItem);
    }
}
