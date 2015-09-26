/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.view.util.menu;

import com.compomics.pladipus.core.control.distribution.service.RunService;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.ProcessDAO;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.RunDAO;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.DeleteOperation;
import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.compomics.pladipus.util.AttachJobAction;
import com.compomics.pladipus.view.panels.impl.UserPanel;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.jms.JMSException;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class RunPopupMenu extends JPopupMenu {

    /**
     * The parenting user panel
     */
    private final UserPanel userPanel;
    /**
     * the target run table
     */
    private final JTable runTable;
    /**
     * the target process table
     */
    private final JTable processTable;
    /**
     * the progress dialog
     */
    private ProgressDialogX progressDialog;
    /**
     * the LOGGING instance
     */
    private static final Logger LOGGER = Logger.getLogger(RunPopupMenu.class);

    public RunPopupMenu(UserPanel userPanel) {
        super();
        this.userPanel = userPanel;
        this.runTable = userPanel.getRunTable();
        this.processTable = userPanel.getProcessTable();
        addAttachJobsAction();
        addStartAction();
        addCancelAction();
        addDeleteAction();
    }

    private void addCancelAction() {
        JMenuItem launchAction = new JMenuItem("Stop Run");
        launchAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = runTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    int dialogResult = JOptionPane.showConfirmDialog(RunPopupMenu.this, "Are you sure you want to pause the selected run(s)?");
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        stopRun(selectedRows);
                    }

                }
            }
        });
        add(launchAction);
    }

    private void addAttachJobsAction() {
        JMenuItem launchAction = new JMenuItem("Attach Jobs");
        launchAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new AttachJobAction().queryUserForJobs(userPanel);
            }
        });
        add(launchAction);
    }

    private void addStartAction() {
        JMenuItem launchAction = new JMenuItem("Start Run");
        launchAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = runTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    int dialogResult = JOptionPane.showConfirmDialog(RunPopupMenu.this, "Are you sure you want to launch the selected run(s)?", "Starting run", JOptionPane.INFORMATION_MESSAGE);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        launchRun(selectedRows);
                    }

                }
            }
        });
        add(launchAction);
    }

    private void addDeleteAction() {
        JMenuItem deleteItem = new JMenuItem("Delete Run");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = runTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    int dialogResult = JOptionPane.showConfirmDialog(RunPopupMenu.this, "Are you sure you want to delete the selected run(s)?");
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        List<Integer> runIDs = new ArrayList<>();
                        for (int selectedRow : selectedRows) {
                            runIDs.add(Integer.parseInt(String.valueOf(runTable.getValueAt(selectedRow, 1))));
                        }
                        try {
                            RunService.getInstance().removeRun(runIDs);
                            userPanel.activate();
                            //update the table
                            DefaultTableModel model = (DefaultTableModel) runTable.getModel();
                            for (int i = selectedRows.length; i > 0; i--) {
                                model.removeRow(selectedRows[i - 1]);
                            }
                            runTable.setModel(model);
                            DefaultTableModel processModel = (DefaultTableModel) processTable.getModel();
                            processModel.setRowCount(0);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(RunPopupMenu.this,
                                    "Could not delete the selection: " + ex,
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } finally {
                            try {
                                userPanel.updateProcessTable();
                                userPanel.updateRunTable();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                //ignore for now?
                            }
                        }
                    }

                }
            }
        });
        add(deleteItem);
    }

    private void launchRun(int[] selectedRows) {
        progressDialog = new ProgressDialogX(true);
        progressDialog.setTitle("Launching jobs. Please Wait...");

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
                ProcessDAO dao = ProcessDAO.getInstance();
                RunDAO rDao = RunDAO.getInstance();
                int runCounter = 0;
                for (int selectedRow : selectedRows) {
                    try {
                        int run_ID = Integer.parseInt(String.valueOf(runTable.getValueAt(selectedRow, 1)));
                        progressDialog.setPrimaryProgressCounterIndeterminate(true);
                        progressDialog.setWaitingText("Getting all unqueued and incomplete processing jobs...");
                        PladipusProcessingTemplate templateForRun = rDao.getTemplateForRun(run_ID);
                        Collection<ProcessingJob> unqueuedProcesses = dao.getJobsForRun(templateForRun, run_ID, false, false);
                        progressDialog.setPrimaryProgressCounterIndeterminate(false);
                        progressDialog.setWaitingText("Starting run ID=" + run_ID + "(" + runCounter + "/" + selectedRows.length + ")");
                        Collection<Integer> processesToQueue = new ArrayList<>();
                        progressDialog.setMaxPrimaryProgressCounter(unqueuedProcesses.size());
                        progressDialog.setPrimaryProgressCounter(0);
                        for (ProcessingJob aJob : unqueuedProcesses) {
                            long processID = aJob.getId();
                            try (
                                    CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.JOB, aJob.toXML(), (int) processID, templateForRun.getPriority())) {
                                producer.run();
                            }
                            processesToQueue.add((int) processID);
                            progressDialog.increasePrimaryProgressCounter();
                        }
                        dao.setQueued(processesToQueue, true);
                    } catch (JMSException | NumberFormatException | SQLException | IOException | StepLoadingException | ParserConfigurationException | SAXException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(RunPopupMenu.this,
                                "Could not launch the selection: " + ex,
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        progressDialog.setRunFinished();
                        return;
                    }
                    runCounter++;
                }
                progressDialog.setRunFinished();
            }
        }.start();
    }

    private void stopRun(int[] selectedRows) {
        progressDialog = new ProgressDialogX(true);
        progressDialog.setPrimaryProgressCounterIndeterminate(false);
        progressDialog.setTitle("Canceling jobs. Please Wait...");

        new Thread(new Runnable() {
            public void run() {
                try {
                    progressDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();

        new Thread("StopThread") {
            @Override
            public void run() {
                ProcessDAO dao = ProcessDAO.getInstance();
                int runCounter = 0;
                for (int selectedRow : selectedRows) {
                    runCounter++;
                    try {
                        int run_ID = Integer.parseInt(String.valueOf(runTable.getValueAt(selectedRow, 1)));
                        progressDialog.setWaitingText("Cancelling run ID=" + run_ID + "(" + runCounter + "/" + selectedRows.length + ")");
                        LinkedList<Integer> queuedProcess = dao.getQueuedProcesses(run_ID);
                        progressDialog.setMaxPrimaryProgressCounter(queuedProcess.size());
                        progressDialog.setPrimaryProgressCounter(0);
                        for (int aProcessID : queuedProcess) {
                            DeleteOperation dO = new DeleteOperation();
                            dO.deleteJobFromQueue(CompomicsQueue.JOB, aProcessID);
                            progressDialog.increasePrimaryProgressCounter();
                        }
                        dao.setQueued(queuedProcess, false);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(RunPopupMenu.this,
                                "Could not launch the selection: " + ex,
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        progressDialog.setRunFinished();
                        return;
                    }
                }
                progressDialog.setRunFinished();
            }
        }.start();
    }

}
