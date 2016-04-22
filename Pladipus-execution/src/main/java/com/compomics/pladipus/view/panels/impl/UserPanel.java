package com.compomics.pladipus.view.panels.impl;

import com.compomics.pladipus.core.control.distribution.service.ProcessService;
import com.compomics.pladipus.core.control.distribution.service.RunService;
import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.RunDAO;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.util.OpenExampleAction;
import com.compomics.pladipus.view.dialogs.run.ProcessCreationDialog;
import com.compomics.pladipus.view.dialogs.run.RunCreationDialog;
import com.compomics.pladipus.view.panels.UpdatingPanel;
import com.compomics.pladipus.view.util.listener.UserTableListSelectionListener;
import com.compomics.pladipus.view.util.menu.ProcessPopupMenu;
import com.compomics.pladipus.view.util.menu.RunPopupMenu;
import com.compomics.pladipus.view.util.renderer.ProgressCellRenderer;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import javax.management.MalformedObjectNameException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class UserPanel extends javax.swing.JPanel implements UpdatingPanel {

    /**
     * The process service instance
     */
    private ProcessService pService = ProcessService.getInstance();
    /**
     * The process service instance
     */
    private RunService rService = RunService.getInstance();
    /**
     * The process service instance
     */
    private String userName;
    /**
     * The process service instance
     */
    private UpdateWorker updateWorker;
    /**
     * The selected run id
     */
    private int selected_run_id = -1;
    /**
     * The template for the selected run
     */
    private PladipusProcessingTemplate runTemplate;
    /**
     * The maximum size for a page
     */
    private int pageSize = 100;
    /**
     * The current page index
     */
    private int currentPage = 1;
    /**
     * The amount of pages needed to display everything
     */
    private int pagesNeeded;
    /**
     * The size of the current selected run (rows) ToDo check if these can be
     * merged
     */
    private int runSize;
    /**
     * The size of the current selected run (rows)
     */
    private int totalRunTableRowCount;
    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(UserPanel.class);

    /**
     * Creates new form UserRunStatistics
     */
    public UserPanel() {
        initComponents();
        //make the tables transparent
        spnlRun.getViewport().setOpaque(false);
        spnlProcess.getViewport().setOpaque(false);
        addTableListener();
        setProgressColumn();
        setPageFieldListener();
    }

    private void setPageFieldListener() {
        tfCurrentPage.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                checkCurrentPageChange();
            }

            ;
            @Override
            public void focusLost(FocusEvent e) {
                checkCurrentPageChange();
            }
        });
    }

    private void setProgressColumn() {
        TableColumnModel rColumnModel = tblRunInfo.getColumnModel();
        TableColumn rColumn = rColumnModel.getColumn(rColumnModel.getColumnCount() - 1);
        rColumn.setCellRenderer(new ProgressCellRenderer());
        TableColumnModel pColumnModel = tblProcessInfo.getColumnModel();
        TableColumn pColumn = pColumnModel.getColumn(pColumnModel.getColumnCount() - 1);
        pColumn.setCellRenderer(new ProgressCellRenderer());
    }

    private void addTableListener() {
        tblRunInfo.getSelectionModel().addListSelectionListener(new UserTableListSelectionListener(this));
        tblProcessInfo.setComponentPopupMenu(new ProcessPopupMenu(this));
        tblRunInfo.setComponentPopupMenu(new RunPopupMenu(this));
    }

    public void setUser(String userName) {
        this.userName = userName;
                try {
            updateRunTable();
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    @Override
    public void activate() {
        updateWorker = new UpdateWorker();
        updateWorker.execute();
    }

    @Override
    public void deactivate() {
        if (updateWorker != null) {
            updateWorker.done();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlRun = new javax.swing.JPanel();
        spnlRun = new javax.swing.JScrollPane();
        tblRunInfo = new javax.swing.JTable();
        btnNewRun = new javax.swing.JButton();
        runHelpLabel = new javax.swing.JLabel();
        openExampleLabel = new javax.swing.JLabel();
        pnlPagination = new javax.swing.JPanel();
        pnlPaginationInput = new javax.swing.JPanel();
        tfCurrentPage = new javax.swing.JTextField();
        lbPages = new javax.swing.JLabel();
        btnNextPage = new javax.swing.JButton();
        btnPrevPage = new javax.swing.JButton();
        pnlProcess = new javax.swing.JPanel();
        spnlProcess = new javax.swing.JScrollPane();
        tblProcessInfo = new javax.swing.JTable();
        btnAddProcess = new javax.swing.JButton();
        processHelpLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        pnlRun.setBackground(new java.awt.Color(255, 255, 255));
        pnlRun.setBorder(javax.swing.BorderFactory.createTitledBorder("My Runs"));

        spnlRun.setBackground(new java.awt.Color(255, 255, 255));

        tblRunInfo.setAutoCreateRowSorter(true);
        tblRunInfo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "R_ID", "Name", "Progress"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblRunInfo.setGridColor(new java.awt.Color(255, 255, 255));
        tblRunInfo.setOpaque(false);
        tblRunInfo.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblRunInfo.getTableHeader().setReorderingAllowed(false);
        spnlRun.setViewportView(tblRunInfo);
        tblRunInfo.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (tblRunInfo.getColumnModel().getColumnCount() > 0) {
            tblRunInfo.getColumnModel().getColumn(0).setMinWidth(50);
            tblRunInfo.getColumnModel().getColumn(0).setPreferredWidth(50);
            tblRunInfo.getColumnModel().getColumn(0).setMaxWidth(50);
            tblRunInfo.getColumnModel().getColumn(1).setMinWidth(50);
            tblRunInfo.getColumnModel().getColumn(1).setPreferredWidth(50);
            tblRunInfo.getColumnModel().getColumn(1).setMaxWidth(50);
            tblRunInfo.getColumnModel().getColumn(3).setMinWidth(200);
            tblRunInfo.getColumnModel().getColumn(3).setPreferredWidth(200);
            tblRunInfo.getColumnModel().getColumn(3).setMaxWidth(200);
        }

        btnNewRun.setText("+");
        btnNewRun.setToolTipText("Create a new run");
        btnNewRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewRunActionPerformed(evt);
            }
        });

        runHelpLabel.setFont(runHelpLabel.getFont().deriveFont((runHelpLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        runHelpLabel.setText("Right click in the table to edit a Run.");

        openExampleLabel.setText("<html>\n<a href=\"\">Open Example Run</a>\n</html>");
        openExampleLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                openExampleLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                openExampleLabelMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                openExampleLabelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout pnlRunLayout = new javax.swing.GroupLayout(pnlRun);
        pnlRun.setLayout(pnlRunLayout);
        pnlRunLayout.setHorizontalGroup(
            pnlRunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRunLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRunLayout.createSequentialGroup()
                        .addComponent(spnlRun)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNewRun)
                        .addContainerGap())
                    .addGroup(pnlRunLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(runHelpLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(openExampleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71))))
        );
        pnlRunLayout.setVerticalGroup(
            pnlRunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRunLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRunLayout.createSequentialGroup()
                        .addComponent(btnNewRun)
                        .addGap(0, 240, Short.MAX_VALUE))
                    .addComponent(spnlRun, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlRunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runHelpLabel)
                    .addComponent(openExampleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pnlPagination.setBackground(new java.awt.Color(255, 255, 255));

        pnlPaginationInput.setBackground(new java.awt.Color(255, 255, 255));
        pnlPaginationInput.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tfCurrentPage.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfCurrentPage.setText("1");
        tfCurrentPage.setBorder(null);
        tfCurrentPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfCurrentPageActionPerformed(evt);
            }
        });

        lbPages.setText("/ 1");

        javax.swing.GroupLayout pnlPaginationInputLayout = new javax.swing.GroupLayout(pnlPaginationInput);
        pnlPaginationInput.setLayout(pnlPaginationInputLayout);
        pnlPaginationInputLayout.setHorizontalGroup(
            pnlPaginationInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPaginationInputLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tfCurrentPage, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbPages)
                .addGap(7, 7, 7))
        );

        pnlPaginationInputLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lbPages, tfCurrentPage});

        pnlPaginationInputLayout.setVerticalGroup(
            pnlPaginationInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPaginationInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(tfCurrentPage, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lbPages, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        btnNextPage.setText(">");
        btnNextPage.setToolTipText("View the next page of processes");
        btnNextPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextPageActionPerformed(evt);
            }
        });

        btnPrevPage.setText("<");
        btnPrevPage.setToolTipText("View the previous page of processes");
        btnPrevPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevPageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlPaginationLayout = new javax.swing.GroupLayout(pnlPagination);
        pnlPagination.setLayout(pnlPaginationLayout);
        pnlPaginationLayout.setHorizontalGroup(
            pnlPaginationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPaginationLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(btnPrevPage, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlPaginationInput, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNextPage)
                .addContainerGap())
        );
        pnlPaginationLayout.setVerticalGroup(
            pnlPaginationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPaginationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlPaginationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnPrevPage)
                    .addComponent(pnlPaginationInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNextPage))
                .addContainerGap())
        );

        pnlProcess.setBackground(new java.awt.Color(255, 255, 255));
        pnlProcess.setBorder(javax.swing.BorderFactory.createTitledBorder("My Processes"));

        spnlProcess.setBackground(new java.awt.Color(255, 255, 255));

        tblProcessInfo.setAutoCreateRowSorter(true);
        tblProcessInfo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "P_ID", "Name", "Progress"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProcessInfo.setGridColor(new java.awt.Color(255, 255, 255));
        tblProcessInfo.setOpaque(false);
        tblProcessInfo.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblProcessInfo.getTableHeader().setReorderingAllowed(false);
        spnlProcess.setViewportView(tblProcessInfo);
        tblProcessInfo.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (tblProcessInfo.getColumnModel().getColumnCount() > 0) {
            tblProcessInfo.getColumnModel().getColumn(0).setMinWidth(50);
            tblProcessInfo.getColumnModel().getColumn(0).setPreferredWidth(50);
            tblProcessInfo.getColumnModel().getColumn(0).setMaxWidth(50);
            tblProcessInfo.getColumnModel().getColumn(1).setMinWidth(50);
            tblProcessInfo.getColumnModel().getColumn(1).setPreferredWidth(50);
            tblProcessInfo.getColumnModel().getColumn(1).setMaxWidth(50);
            tblProcessInfo.getColumnModel().getColumn(3).setMinWidth(200);
            tblProcessInfo.getColumnModel().getColumn(3).setPreferredWidth(200);
            tblProcessInfo.getColumnModel().getColumn(3).setMaxWidth(200);
        }

        btnAddProcess.setText("+");
        btnAddProcess.setToolTipText("Add processes to an existing run");
        btnAddProcess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProcessActionPerformed(evt);
            }
        });

        processHelpLabel.setFont(processHelpLabel.getFont().deriveFont((processHelpLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        processHelpLabel.setText("Right click in the table to edit a Process.");

        javax.swing.GroupLayout pnlProcessLayout = new javax.swing.GroupLayout(pnlProcess);
        pnlProcess.setLayout(pnlProcessLayout);
        pnlProcessLayout.setHorizontalGroup(
            pnlProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProcessLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProcessLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(processHelpLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlProcessLayout.createSequentialGroup()
                        .addComponent(spnlProcess, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddProcess)
                        .addContainerGap())))
        );
        pnlProcessLayout.setVerticalGroup(
            pnlProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProcessLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProcessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProcessLayout.createSequentialGroup()
                        .addComponent(btnAddProcess)
                        .addGap(0, 240, Short.MAX_VALUE))
                    .addComponent(spnlProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processHelpLabel)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(pnlPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlProcess, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProcess, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlPagination, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showCreateNewRunDialog() throws Exception {
        try {
            RunCreationDialog dialog = new RunCreationDialog(null, userName, this, true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            PladipusProcessingTemplate processingTemplate = dialog.getProcessingTemplate();
            if (dialog.isConfirmed()) {
                RunDAO rInstance = RunDAO.getInstance();
                int runID = rInstance.createRun(processingTemplate);
            }
            dialog.dispose();
            updateRunTable();
            updateProcessTable();
        } catch (SQLException | ParserConfigurationException | IOException | SAXException ex) {
            ex.printStackTrace();
        }
    }
    private void btnNewRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewRunActionPerformed
        try {
            showCreateNewRunDialog();
        } catch (Exception ex) {
           LOGGER.error("Couldn't update tables...");
           ex.printStackTrace();
        }
    }//GEN-LAST:event_btnNewRunActionPerformed

    private void btnAddProcessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProcessActionPerformed
        //check only one run is selected
        int[] selectedRows = tblRunInfo.getSelectedRows();
        if (selectedRows.length <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a Run to add the Processes to.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            Integer runID = Integer.parseInt(String.valueOf(tblRunInfo.getValueAt(tblRunInfo.getSelectedRow(), 1)));
            try {
                PladipusProcessingTemplate templateForRun = rService.getTemplateForRun(runID);
                ProcessCreationDialog processCreationDialog = new ProcessCreationDialog(null, true, templateForRun);
                processCreationDialog.setLocationRelativeTo(this);
                processCreationDialog.setVisible(true);
            } catch (SQLException | IOException | ProcessStepInitialisationException | ParserConfigurationException | SAXException ex) {
                ex.printStackTrace();
            }
        }
        try {
            ((DefaultTableModel) tblRunInfo.getModel()).setRowCount(0);
            //update tables
            updateRunTable();
            updateProcessTable();
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }//GEN-LAST:event_btnAddProcessActionPerformed

    private void btnNextPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextPageActionPerformed
        if (currentPage < (pagesNeeded)) {
            currentPage++;
            tfCurrentPage.setText(String.valueOf(currentPage));
            try {
                updateProcessTable();
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }
    }//GEN-LAST:event_btnNextPageActionPerformed

    private void btnPrevPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevPageActionPerformed
        if (currentPage > 1) {
            currentPage--;
            tfCurrentPage.setText(String.valueOf(currentPage));
            try {
                updateProcessTable();
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }
    }//GEN-LAST:event_btnPrevPageActionPerformed

    private void tfCurrentPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfCurrentPageActionPerformed
        checkCurrentPageChange();
    }//GEN-LAST:event_tfCurrentPageActionPerformed

    private void openExampleLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openExampleLabelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_openExampleLabelMouseEntered

    private void openExampleLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openExampleLabelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_openExampleLabelMouseExited

    private void openExampleLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openExampleLabelMouseReleased
        new OpenExampleAction().openExample(userName, this);
    }//GEN-LAST:event_openExampleLabelMouseReleased

    private void checkCurrentPageChange() {
        try {
            int parseInt = Integer.parseInt(tfCurrentPage.getText());
            if (parseInt <= 0 || parseInt > pagesNeeded) {
                tfCurrentPage.setText("1");
                promptErrorMessage();
            } else if (currentPage != parseInt) {
                currentPage = parseInt;
                updateProcessTable();
            }
        } catch (NumberFormatException e) {
            tfCurrentPage.setText("1");
            promptErrorMessage();
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    private void promptErrorMessage() {
        JOptionPane.showMessageDialog(null,
                "Please enter number between 1 and " + pagesNeeded + ".", "Input Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void reloadPage() {

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProcess;
    private javax.swing.JButton btnNewRun;
    private javax.swing.JButton btnNextPage;
    private javax.swing.JButton btnPrevPage;
    private javax.swing.JLabel lbPages;
    private javax.swing.JLabel openExampleLabel;
    private javax.swing.JPanel pnlPagination;
    private javax.swing.JPanel pnlPaginationInput;
    private javax.swing.JPanel pnlProcess;
    private javax.swing.JPanel pnlRun;
    private javax.swing.JLabel processHelpLabel;
    private javax.swing.JLabel runHelpLabel;
    private javax.swing.JScrollPane spnlProcess;
    private javax.swing.JScrollPane spnlRun;
    private javax.swing.JTable tblProcessInfo;
    private javax.swing.JTable tblRunInfo;
    private javax.swing.JTextField tfCurrentPage;
    // End of variables declaration//GEN-END:variables

    public JTable getRunTable() {
        return tblRunInfo;
    }

    public JTable getProcessTable() {
        return tblProcessInfo;
    }

    public void updateRunTable() throws IOException, MalformedObjectNameException, Exception {
        JTable table = UserPanel.this.tblRunInfo;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        if (model == null) {
            model = new DefaultTableModel();
            totalRunTableRowCount = 10;
        }
        TreeMap<Integer, String> runs = rService.getRuns(userName);
        totalRunTableRowCount = runs.size();
        if (totalRunTableRowCount > 0) {
            model.setRowCount(totalRunTableRowCount);
            int[] selectedRunRows = tblRunInfo.getSelectedRows();
            int row = 0;
            int index = 1;
            for (Map.Entry<Integer, String> run_id : runs.entrySet()) {
                //set the values in the existing sells
                try {
                    double completeProcessCount = pService.getCompleteProcessCount(run_id.getKey());
                    model.setValueAt(index, row, 0);
                    model.setValueAt(run_id.getKey(), row, 1);
                    model.setValueAt(run_id.getValue(), row, 2);
                    model.setValueAt(completeProcessCount, row, 3);
                    index++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                row++;
            }
            for (int selectedRowIndex : selectedRunRows) {
                tblRunInfo.addRowSelectionInterval(selectedRowIndex, selectedRowIndex);
            }
            table.setModel(model);
            totalRunTableRowCount = runs.size();
            model.setRowCount(totalRunTableRowCount);
        }
    }

    public void updateProcessTable() throws Exception {
        if (runTemplate != null && tblRunInfo.getSelectedRowCount() == 1) {
            DefaultTableModel tableModel = (DefaultTableModel) tblProcessInfo.getModel();
            int totalStepSize = runTemplate.getProcessingSteps().size();
            int[] selectedProcessRows = tblProcessInfo.getSelectedRows();
            if (runSize > 0) {
                pagesNeeded = 0;
                if (runSize % pageSize > 0) {
                    pagesNeeded++;
                }
                pagesNeeded += (runSize / pageSize);

                lbPages.setText("/ " + (pagesNeeded));
                int lowerlimit = (currentPage - 1) * pageSize;
                int index = lowerlimit;

                LinkedList<Object[]> processInformation = pService.getProcessInformation(selected_run_id, lowerlimit, pageSize);
                tableModel.setNumRows(processInformation.size());
                int row = 0;

                for (Object[] anObjectArray : processInformation) {
                    index++;
                    long processId = (Long) anObjectArray[0];
                    String state = String.valueOf(anObjectArray[1]);
                    int stepCount = (Integer) anObjectArray[2];
                    boolean completed = (Boolean) anObjectArray[3];
                    int completionPercent = (int) Math.round(100 * (double) stepCount / (double) totalStepSize);
                    tableModel.setValueAt(index, row, 0);
                    tableModel.setValueAt(processId, row, 1);
                    tableModel.setValueAt(state, row, 2);
                    if (completed) {
                        tableModel.setValueAt(100.0, row, 3);
                    } else {
                        tableModel.setValueAt(Math.min(99, completionPercent), row, 3);
                    }
                    row++;
                }
                if (selectedProcessRows.length > 0) {
                    for (int selectedRowIndex : selectedProcessRows) {
                        tblProcessInfo.addRowSelectionInterval(selectedRowIndex, selectedRowIndex);
                    }
                }
            } else {
                tableModel.setRowCount(0);
            }
            tblProcessInfo.setModel(tableModel);
        }
    }

    public void setSelectedRunId(int run_id) {
        this.selected_run_id = run_id;
        //   ((DefaultTableModel) tblProcessInfo.getModel()).setRowCount(100);
        try {
            this.runTemplate = rService.getTemplateForRun(selected_run_id);
            this.runSize = rService.getRunSize(selected_run_id);
            lbPages.setText("/" + runSize);
            currentPage = 1;
            tfCurrentPage.setText(String.valueOf(currentPage));

        } catch (SQLException | IOException | ProcessStepInitialisationException | ParserConfigurationException | SAXException ex) {
            //ignore for now?
            ex.printStackTrace();
        }
    }

    private class UpdateWorker extends SwingWorker<Integer, Integer> {

        private boolean isDone = false;

        @Override
        protected Integer doInBackground() throws Exception {
            while (!isDone) {
                try {

                    Thread.sleep(1000);
                    if (selected_run_id > 0) {
                        updateRunTable();
                        updateProcessTable();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
            return 0;
        }

        @Override
        protected void done() {
            isDone = true;
        }

    }

}
