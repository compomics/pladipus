package com.compomics.pladipus.view.dialogs.run;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.updates.ProcessingBeanUpdater;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.prerequisite.Prerequisite;
import com.compomics.pladipus.core.model.prerequisite.PrerequisiteParameter;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.processing.templates.ProcessingParameterTemplate;
import com.compomics.pladipus.view.panels.impl.UserPanel;
import com.compomics.pladipus.view.util.renderer.xmlEditorKit.XMLEditorKit;
import com.compomics.util.gui.renderers.AlignedListCellRenderer;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import no.uib.jsparklines.extra.NimbusCheckBoxRenderer;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class RunCreationDialog extends javax.swing.JDialog {
    
    private static final Logger LOGGER = Logger.getLogger(RunCreationDialog.class);
    /**
     * the run owner
     */
    private final String user;
    /**
     * the temporary template to display
     */
    private PladipusProcessingTemplate template;
    /**
     *
     * the template file chooser
     */
    private final JFileChooser fileChooser = new JFileChooser();
    /**
     * the last selected folder
     */
    private File lastSelectedFolder = new File(System.getProperty("user.home") + "/pladipus");
    /**
     * the installed processes file chooser
     */
    private final TreeMap<String, String> installedProcessStepClasses;
    /**
     * the preference to send the jobs for this run to
     */
    private Prerequisite prerequisite = new Prerequisite();
    /**
     * boolean indicating the run can be added to the database
     */
    private boolean confirmed;
    /**
     * the active userPanel
     */
    private UserPanel userPanel;
    /**
     * the cached preset templates (XML format)
     */
    private LinkedHashMap<String, File> presets;
    private XMLTemplateInterpreter interpeter;

    /**
     * Creates new form RunCreationDialog
     */
    public RunCreationDialog(java.awt.Frame parent, String user, UserPanel userPanel, boolean modal
    ) throws ParserConfigurationException, IOException, SAXException {
        super(parent, modal);
        initComponents();
        this.userPanel = userPanel;
        this.user = user;
        this.setTitle("Run Creation Wizard");
        
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/pladipus_icon.gif")));
        
        tblParameters.getTableHeader().setReorderingAllowed(false);
        
        tblParameters.getColumn(" ").setMaxWidth(40);
        tblParameters.getColumn(" ").setMinWidth(40);
        
        tblParameters.getColumn("Run*").setMaxWidth(50);
        tblParameters.getColumn("Run*").setMinWidth(50);
        
        tblParameters.getColumn("Run*").setCellRenderer(new NimbusCheckBoxRenderer());
        
        cbPresets.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));
        cbSteps.setRenderer(new AlignedListCellRenderer(SwingConstants.CENTER));

        //make the panels opaque
        spnlPreview.getViewport().setOpaque(false);
        spnlParameters.getViewport().setOpaque(false);

        //preview xmlEditorKit
        epnlPreviewXML.setEditorKit(new XMLEditorKit());
        epnlPreviewXML.setEditable(true);
        //step loading
        ProcessingBeanUpdater updater = ProcessingBeanUpdater.getInstance();
        installedProcessStepClasses = updater.getInstalledProcessStepClasses();

        //fill parameters
        tblParameters.getModel().addTableModelListener(
                new TableModelListener() {
            public void tableChanged(TableModelEvent evt) {
                //read all the table values, turn them into proper parameters and fling them into the model, then refresh
                DefaultTableModel model = (DefaultTableModel) tblParameters.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    try {
                        String parameterName = String.valueOf(model.getValueAt(i, 1));
                        if (!parameterName.isEmpty()) {
                            String parameterValue;
                            if (model.getValueAt(i, 2) != null) {
                                parameterValue = String.valueOf(model.getValueAt(i, 2));
                            } else {
                                parameterValue = "";
                            }
                            boolean runParameter;
                            if (model.getValueAt(i, 4) != null) {
                                runParameter = (Boolean) model.getValueAt(i, 4);
                            } else {
                                runParameter = false;
                            }
                            ProcessingParameterTemplate processingParameter = new ProcessingParameterTemplate(parameterName, parameterValue);
                            template.getRunParameters().remove(parameterName);
                            template.getJobParameters().remove(parameterName);
                            if (runParameter) {
                                template.addRunParameter(processingParameter);
                            } else {
                                template.addJobParameter(processingParameter);
                            }
                        }
                    } catch (NullPointerException e) {
                        //todo refactor to avoid this
                    }
                }
                refreshPreview();
            }
        });
        
        tfRunName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent arg0) {
                super.focusLost(arg0);
                String text = tfRunName.getText();
                template.setName(text);
                refreshPreview();
            }
        });

        //combobox
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("-- Select --");
        for (String aClass : installedProcessStepClasses.keySet()) {
            if (!aClass.toLowerCase().contains("testing")) {
                model.addElement(aClass);
            }
        }
        cbSteps.setModel(model);
        
        liSteps.setModel(new DefaultListModel());
        
        template = new PladipusProcessingTemplate("Default Run Name", user, 4, prerequisite);

        //add prerequisite buttons
        btnGroupOSArch.add(rdbLinux32);
        btnGroupOSArch.add(rdbLinux64);
        btnGroupOSArch.add(rdbWindows32);
        btnGroupOSArch.add(rdbWindows64);
        
        refreshPreview();
        try {
            loadPresets();
        } catch (ProcessStepInitialisationException ex) {
            JOptionPane.showMessageDialog(this, "Template Error", "An error occurred loading a preset template: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setTemplateFromResource(String templateIdentifier, String templateFileName) throws IOException, ParserConfigurationException, ProcessStepInitialisationException, SAXException {
        File tempFile = File.createTempFile(templateIdentifier, ".temp");
        tempFile.deleteOnExit();
        try (OutputStream out = new FileOutputStream(tempFile);
                InputStream in = getClass().getClassLoader().getResource("templates/" + templateFileName).openStream()) {
            IOUtils.copy(in, out);
            presets.put(templateIdentifier, tempFile);
        }
    }
    
    private void loadPresets() throws IOException, ParserConfigurationException, ProcessStepInitialisationException, SAXException {
        LOGGER.debug("Loading presets...");
        presets = new LinkedHashMap<>();
        interpeter = XMLTemplateInterpreter.getInstance();
        setTemplateFromResource("SearchGUI", "SearchGUI_Template.xml");
        setTemplateFromResource("PeptideShaker", "PeptideShaker_Template.xml");
        setTemplateFromResource("SearchGUI + PeptideShaker", "SearchGUI_PeptideShaker_Template.xml");
        setTemplateFromResource("DeNovoGUI", "DeNovoGUI_Template.xml");
        setTemplateFromResource("BLAST", "BLAST_Template.xml");
        setTemplateFromResource("MsConvert", "MsConvert_Template.xml");
        setTemplateFromResource("Custom", "Empty_Template.xml");
        
        DefaultComboBoxModel model = (DefaultComboBoxModel) cbPresets.getModel();
        model.removeAllElements();
        model.addElement("-- Select --");
        for (String aPreset : presets.keySet()) {
            model.addElement(aPreset);
        }
        cbPresets.setModel(model);
        LOGGER.debug("Done...");
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGroupOSArch = new javax.swing.ButtonGroup();
        stepsPopupMenu = new javax.swing.JPopupMenu();
        moveUpMenuItem = new javax.swing.JMenuItem();
        removeMenuItem = new javax.swing.JMenuItem();
        moveDownMenuItem = new javax.swing.JMenuItem();
        pnlMain = new javax.swing.JPanel();
        btnCreateRun = new javax.swing.JButton();
        pnlSteps = new javax.swing.JPanel();
        liSteps = new javax.swing.JList();
        cbSteps = new javax.swing.JComboBox();
        lbPreSet = new javax.swing.JLabel();
        lbPreSet1 = new javax.swing.JLabel();
        cbPresets = new javax.swing.JComboBox();
        pnlRunName = new javax.swing.JPanel();
        tfRunName = new javax.swing.JTextField();
        lblPriority = new javax.swing.JLabel();
        slPriority = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        cbRetainJobOrder = new javax.swing.JCheckBox();
        pnlParameters = new javax.swing.JPanel();
        spnlParameters = new javax.swing.JScrollPane();
        tblParameters = new javax.swing.JTable();
        btnAddParameter = new javax.swing.JButton();
        btnRemoveParameter = new javax.swing.JButton();
        lblParameterRemark = new javax.swing.JLabel();
        pnlPreview = new javax.swing.JPanel();
        spnlPreview = new javax.swing.JScrollPane();
        epnlPreviewXML = new javax.swing.JEditorPane();
        btnCancel = new javax.swing.JButton();
        mbMain = new javax.swing.JMenuBar();
        miFile = new javax.swing.JMenu();
        miImport = new javax.swing.JMenuItem();
        miExit = new javax.swing.JMenuItem();
        miPreferences = new javax.swing.JMenu();
        miClearPreferences = new javax.swing.JMenuItem();
        sprOS = new javax.swing.JPopupMenu.Separator();
        rdbWindows64 = new javax.swing.JRadioButtonMenuItem();
        rdbWindows32 = new javax.swing.JRadioButtonMenuItem();
        rdbLinux64 = new javax.swing.JRadioButtonMenuItem();
        rdbLinux32 = new javax.swing.JRadioButtonMenuItem();
        rdbNoOs = new javax.swing.JRadioButtonMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        miSetCores = new javax.swing.JMenuItem();
        miSetMemory = new javax.swing.JMenuItem();
        miSetDiskSpace = new javax.swing.JMenuItem();

        moveUpMenuItem.setText("Move Up");
        moveUpMenuItem.setToolTipText("");
        moveUpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpMenuItemActionPerformed(evt);
            }
        });
        stepsPopupMenu.add(moveUpMenuItem);

        removeMenuItem.setText("Remove");
        removeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMenuItemActionPerformed(evt);
            }
        });
        stepsPopupMenu.add(removeMenuItem);

        moveDownMenuItem.setText("Move Down");
        moveDownMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownMenuItemActionPerformed(evt);
            }
        });
        stepsPopupMenu.add(moveDownMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlMain.setBackground(new java.awt.Color(255, 255, 255));

        btnCreateRun.setText("Create");
        btnCreateRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateRunActionPerformed(evt);
            }
        });

        pnlSteps.setBackground(new java.awt.Color(255, 255, 255));
        pnlSteps.setBorder(javax.swing.BorderFactory.createTitledBorder("Steps"));

        liSteps.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        liSteps.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        liSteps.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                liStepsMouseReleased(evt);
            }
        });

        cbSteps.setMaximumRowCount(15);
        cbSteps.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbSteps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cbStepsFocusLost(evt);
            }
        });
        cbSteps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStepsActionPerformed(evt);
            }
        });

        lbPreSet.setText("Presets");

        lbPreSet1.setText("Steps");

        cbPresets.setMaximumRowCount(15);
        cbPresets.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SearchGUI", "SearchGUI + PeptideShaker", "BlastP", "BlastN", "BlastX" }));
        cbPresets.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cbPresetsFocusLost(evt);
            }
        });
        cbPresets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbPresetsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlStepsLayout = new javax.swing.GroupLayout(pnlSteps);
        pnlSteps.setLayout(pnlStepsLayout);
        pnlStepsLayout.setHorizontalGroup(
            pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStepsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(liSteps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlStepsLayout.createSequentialGroup()
                        .addGroup(pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbPreSet, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbPreSet1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbSteps, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbPresets, 0, 382, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlStepsLayout.setVerticalGroup(
            pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlStepsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbPresets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbPreSet))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSteps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbPreSet1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(liSteps, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        pnlRunName.setBackground(new java.awt.Color(255, 255, 255));
        pnlRunName.setBorder(javax.swing.BorderFactory.createTitledBorder("Run"));

        tfRunName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfRunName.setText("Default Run Name");
        tfRunName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfRunNameActionPerformed(evt);
            }
        });

        lblPriority.setText("Run Priority");

        slPriority.setBackground(new java.awt.Color(255, 255, 255));
        slPriority.setForeground(new java.awt.Color(102, 204, 255));
        slPriority.setMaximum(9);
        slPriority.setMinimum(1);
        slPriority.setMinorTickSpacing(1);
        slPriority.setPaintLabels(true);
        slPriority.setPaintTicks(true);
        slPriority.setSnapToTicks(true);
        slPriority.setValue(5);

        jLabel1.setText("Run Name");

        cbRetainJobOrder.setBackground(new java.awt.Color(255, 255, 255));
        cbRetainJobOrder.setText("Retain job order");
        cbRetainJobOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbRetainJobOrderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlRunNameLayout = new javax.swing.GroupLayout(pnlRunName);
        pnlRunName.setLayout(pnlRunNameLayout);
        pnlRunNameLayout.setHorizontalGroup(
            pnlRunNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRunNameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRunNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbRetainJobOrder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlRunNameLayout.createSequentialGroup()
                        .addGroup(pnlRunNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPriority, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlRunNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(slPriority, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                            .addComponent(tfRunName))))
                .addContainerGap())
        );
        pnlRunNameLayout.setVerticalGroup(
            pnlRunNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRunNameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRunNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfRunName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(pnlRunNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(slPriority, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRunNameLayout.createSequentialGroup()
                        .addComponent(lblPriority)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(cbRetainJobOrder)
                .addContainerGap())
        );

        pnlParameters.setBackground(new java.awt.Color(255, 255, 255));
        pnlParameters.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));

        spnlParameters.setBackground(new java.awt.Color(255, 255, 255));
        spnlParameters.setOpaque(false);

        tblParameters.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Parameter", "Value", "Description", "Run*"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblParameters.setOpaque(false);
        tblParameters.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        spnlParameters.setViewportView(tblParameters);

        btnAddParameter.setText("+");
        btnAddParameter.setToolTipText("Add parameter");
        btnAddParameter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddParameterActionPerformed(evt);
            }
        });

        btnRemoveParameter.setText("-");
        btnRemoveParameter.setToolTipText("Remove parameter");
        btnRemoveParameter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveParameterActionPerformed(evt);
            }
        });

        lblParameterRemark.setFont(lblParameterRemark.getFont().deriveFont((lblParameterRemark.getFont().getStyle() | java.awt.Font.ITALIC)));
        lblParameterRemark.setText("* Run parameters will be available to all Jobs in the template");

        javax.swing.GroupLayout pnlParametersLayout = new javax.swing.GroupLayout(pnlParameters);
        pnlParameters.setLayout(pnlParametersLayout);
        pnlParametersLayout.setHorizontalGroup(
            pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spnlParameters)
                    .addGroup(pnlParametersLayout.createSequentialGroup()
                        .addComponent(lblParameterRemark)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 637, Short.MAX_VALUE)
                        .addComponent(btnAddParameter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveParameter, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pnlParametersLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAddParameter, btnRemoveParameter});

        pnlParametersLayout.setVerticalGroup(
            pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlParameters, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblParameterRemark)
                    .addComponent(btnAddParameter)
                    .addComponent(btnRemoveParameter))
                .addGap(4, 4, 4))
        );

        pnlPreview.setBackground(new java.awt.Color(255, 255, 255));
        pnlPreview.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));

        spnlPreview.setBorder(null);

        epnlPreviewXML.setBorder(null);
        spnlPreview.setViewportView(epnlPreviewXML);

        javax.swing.GroupLayout pnlPreviewLayout = new javax.swing.GroupLayout(pnlPreview);
        pnlPreview.setLayout(pnlPreviewLayout);
        pnlPreviewLayout.setHorizontalGroup(
            pnlPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPreviewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlPreview, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlPreviewLayout.setVerticalGroup(
            pnlPreviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPreviewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(spnlPreview)
                .addContainerGap())
        );

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pnlParameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnlMainLayout.createSequentialGroup()
                                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(pnlRunName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(pnlSteps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCreateRun)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel)))
                .addContainerGap())
        );

        pnlMainLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCancel, btnCreateRun});

        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMainLayout.createSequentialGroup()
                        .addComponent(pnlRunName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlSteps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlParameters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreateRun)
                    .addComponent(btnCancel))
                .addGap(16, 16, 16))
        );

        miFile.setText("File");

        miImport.setText("Import...");
        miImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miImportActionPerformed(evt);
            }
        });
        miFile.add(miImport);

        miExit.setText("Exit");
        miExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miExitActionPerformed(evt);
            }
        });
        miFile.add(miExit);

        mbMain.add(miFile);

        miPreferences.setText("Preferences");

        miClearPreferences.setText("Clear All Preferences");
        miClearPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miClearPreferencesActionPerformed(evt);
            }
        });
        miPreferences.add(miClearPreferences);
        miPreferences.add(sprOS);

        rdbWindows64.setText("Windows 64-bit");
        rdbWindows64.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbWindows64ActionPerformed(evt);
            }
        });
        miPreferences.add(rdbWindows64);

        rdbWindows32.setText("Windows 32-bit");
        rdbWindows32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbWindows32ActionPerformed(evt);
            }
        });
        miPreferences.add(rdbWindows32);

        rdbLinux64.setText("Linux 64-bit");
        rdbLinux64.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbLinux64ActionPerformed(evt);
            }
        });
        miPreferences.add(rdbLinux64);

        rdbLinux32.setText("Linux 32-bit");
        rdbLinux32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbLinux32ActionPerformed(evt);
            }
        });
        miPreferences.add(rdbLinux32);

        rdbNoOs.setText("None");
        rdbNoOs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbNoOsActionPerformed(evt);
            }
        });
        miPreferences.add(rdbNoOs);
        miPreferences.add(jSeparator2);

        miSetCores.setText("Set Minimal Cores (1)");
        miSetCores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSetCoresActionPerformed(evt);
            }
        });
        miPreferences.add(miSetCores);

        miSetMemory.setText("Set Minimal RAM (0 GB)");
        miSetMemory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSetMemoryActionPerformed(evt);
            }
        });
        miPreferences.add(miSetMemory);

        miSetDiskSpace.setText("Set Minimal Disk (0 GB)");
        miSetDiskSpace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSetDiskSpaceActionPerformed(evt);
            }
        });
        miPreferences.add(miSetDiskSpace);

        mbMain.add(miPreferences);

        setJMenuBar(mbMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void refreshPreview() {
        LOGGER.debug("Refreshing preview...");
        if (template != null) {
            template.setUser(user);
            //check the step order
            template.clearProcessSteps();
            DefaultListModel model = (DefaultListModel) liSteps.getModel();
            Enumeration elements = model.elements();
            while (elements.hasMoreElements()) {
                template.addProcessingStep(installedProcessStepClasses.get(String.valueOf(elements.nextElement())));
            }
            template.setPriority(slPriority.getValue());
            epnlPreviewXML.setEditorKit(new XMLEditorKit());
            epnlPreviewXML.setText(template.toXML());
            epnlPreviewXML.setCaretPosition(0);
        }
        LOGGER.debug("Done...");
    }

    private void btnCreateRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateRunActionPerformed

        // check that there is at least one job parameter
        boolean jobParameterFound = false;
        
        for (int i = 0; i < tblParameters.getRowCount() && !jobParameterFound; i++) {
            if (!((Boolean) tblParameters.getValueAt(i, 4))) {
                jobParameterFound = true;
            }
        }
        
        if (!jobParameterFound) {
            JOptionPane.showMessageDialog(this,
                    "You need to have at least one Job specific parameter.",
                    "Input Warning",
                    JOptionPane.WARNING_MESSAGE);
        } else {

            //save the XML to a file = start a filechooser?
            fileChooser.setCurrentDirectory(lastSelectedFolder);
            fileChooser.setDialogTitle("Specify Output File");
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().endsWith(".xml")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".xml");
                }
                lastSelectedFolder = fileToSave.getParentFile();
                if (fileToSave.exists()) {
                    int dialogResult = JOptionPane.showConfirmDialog(this, fileToSave.getName() + " already exists, do you want to overwrite the file?");
                    if (dialogResult != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                try (FileWriter xmlOut = new FileWriter(fileToSave);
                        FileWriter tsvOut = new FileWriter(new File(fileToSave.getAbsolutePath() + ".tsv"))) {
                    //set the username
                    template.setUser(user);
                    //save the template
                    xmlOut.append(template.toXML().replace(">", ">" + System.lineSeparator())).flush();
                    if (template.getAllProcessingParameters().size() > 0) {
                        //save the parameter configuration tsv file
                        StringBuilder headers = new StringBuilder();
                        for (String aHeader : template.getJobParameters().keySet()) {
                            headers.append(aHeader).append("\t");
                        }
                        //remove the last tab
                        headers.setLength(headers.length() - 1);
                        headers.append(System.lineSeparator());
                        tsvOut.append(headers).flush();
                    }
                    this.setVisible(false);
                    confirmed = true;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "An error occurred during exporting: " + ex.getMessage(),
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_btnCreateRunActionPerformed
    

    private void tfRunNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfRunNameActionPerformed
        refreshPreview();
    }//GEN-LAST:event_tfRunNameActionPerformed

    private void miExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miExitActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_miExitActionPerformed
    
    private void clearOSArchPrerequisites() {
        prerequisite.removePrerequisite(PrerequisiteParameter.OS_WINDOWS);
        rdbLinux32.setSelected(false);
        rdbWindows32.setSelected(false);
        rdbLinux64.setSelected(false);
        rdbWindows64.setSelected(false);
        prerequisite.removePrerequisite(PrerequisiteParameter.OS_LINUX);
        prerequisite.removePrerequisite(PrerequisiteParameter.ARCH_64);
        prerequisite.removePrerequisite(PrerequisiteParameter.ARCH_32);
    }
    

    private void rdbWindows64ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbWindows64ActionPerformed
        clearOSArchPrerequisites();
        if (rdbWindows64.isSelected()) {
            prerequisite.addPrerequisite(PrerequisiteParameter.OS_WINDOWS);
            prerequisite.addPrerequisite(PrerequisiteParameter.ARCH_64);
        }
        refreshPreview();
    }//GEN-LAST:event_rdbWindows64ActionPerformed

    private void rdbWindows32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbWindows32ActionPerformed
        clearOSArchPrerequisites();
        if (rdbWindows32.isSelected()) {
            prerequisite.addPrerequisite(PrerequisiteParameter.OS_WINDOWS);
            prerequisite.addPrerequisite(PrerequisiteParameter.ARCH_32);
        }
        refreshPreview();
    }//GEN-LAST:event_rdbWindows32ActionPerformed

    private void rdbLinux64ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbLinux64ActionPerformed
        clearOSArchPrerequisites();
        if (rdbLinux64.isSelected()) {
            prerequisite.addPrerequisite(PrerequisiteParameter.OS_LINUX);
            prerequisite.addPrerequisite(PrerequisiteParameter.ARCH_64);
        }
        refreshPreview();
    }//GEN-LAST:event_rdbLinux64ActionPerformed

    private void rdbLinux32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbLinux32ActionPerformed
        clearOSArchPrerequisites();
        if (rdbLinux32.isSelected()) {
            prerequisite.addPrerequisite(PrerequisiteParameter.OS_LINUX);
            prerequisite.addPrerequisite(PrerequisiteParameter.ARCH_32);
        }
        refreshPreview();
    }//GEN-LAST:event_rdbLinux32ActionPerformed

    private void miSetCoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSetCoresActionPerformed
        String message = JOptionPane.showInputDialog(null, "Please specify the minimal amount of cores");
        try {
            int cores = Integer.parseInt(message);
            prerequisite.removePrerequisite(PrerequisiteParameter.CORES);
            prerequisite.addPrerequisite(PrerequisiteParameter.CORES, message);
            if (cores == -1) {
                miSetCores.setText("Set Minimal Cores");
            }
            miSetCores.setText("Set Minimal Cores (" + cores + ")");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please insert a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
        refreshPreview();
    }//GEN-LAST:event_miSetCoresActionPerformed

    private void miSetMemoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSetMemoryActionPerformed
        String message = JOptionPane.showInputDialog(null, "Please specify the minimal amount of RAM (in GB)");
        try {
            long RAM = Long.parseLong(message);
            prerequisite.removePrerequisite(PrerequisiteParameter.MEMORY);
            prerequisite.addPrerequisite(PrerequisiteParameter.MEMORY, String.valueOf(RAM));
            if (RAM == -1) {
                miSetMemory.setText("Set Minimal RAM");
            }
            miSetMemory.setText("Set Minimal RAM (" + message + " GB)");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please insert a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
        refreshPreview();
    }//GEN-LAST:event_miSetMemoryActionPerformed

    private void miSetDiskSpaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSetDiskSpaceActionPerformed
        String message = JOptionPane.showInputDialog(null, "Please specify the minimal amount of disk space (in GB)");
        try {
            long diskSpace = Long.parseLong(message);
            prerequisite.removePrerequisite(PrerequisiteParameter.DISKSPACE);
            prerequisite.addPrerequisite(PrerequisiteParameter.DISKSPACE, String.valueOf(diskSpace));
            if (diskSpace == -1) {
                miSetDiskSpace.setText("Set Minimal Disk");
            }
            miSetDiskSpace.setText("Set Minimal Disk (" + message + " GB)");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please insert a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
        refreshPreview();
    }//GEN-LAST:event_miSetDiskSpaceActionPerformed

    private void miClearPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miClearPreferencesActionPerformed
        for (PrerequisiteParameter aParamter : PrerequisiteParameter.values()) {
            prerequisite.removePrerequisite(aParamter);
        }
        miSetDiskSpace.setText("Set Minimal Disk");
        miSetMemory.setText("Set Minimal RAM");
        miSetCores.setText("Set Minimal Cores");
        refreshPreview();
    }//GEN-LAST:event_miClearPreferencesActionPerformed
    
    private void reloadTemplate(PladipusProcessingTemplate presetTemplate) {
        //check for unknown steps
        LOGGER.debug("Refreshing template...");
        ArrayList<String> unknownSteps = new ArrayList<>();
        for (String aStep : presetTemplate.getProcessingSteps()) {
            if (!installedProcessStepClasses.values().contains(aStep)) {
                unknownSteps.add(aStep);
            }
        }
        
        if (!unknownSteps.isEmpty()) {
            StringBuilder unknownStepMessage = new StringBuilder("The following classes are not correctly installed : ").append(System.lineSeparator());
            for (String anUnknownStep : unknownSteps) {
                unknownStepMessage.append(anUnknownStep).append(System.lineSeparator());
            }
            JOptionPane.showMessageDialog(this,
                    unknownStepMessage.toString(),
                    "Your import seems corrupted...",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        synchronized (template) {
            //set the lists and table and run name

            DefaultListModel liModel = (DefaultListModel) liSteps.getModel();
            DefaultTableModel tbModel = (DefaultTableModel) tblParameters.getModel();
            
            tfRunName.setText(presetTemplate.getName());
            liModel.removeAllElements();
            for (String aStep : presetTemplate.getProcessingSteps()) {
                liModel.addElement(aStep.substring(aStep.lastIndexOf(".") + 1));
            }
            tbModel.setRowCount(0);
            
            TreeMap<String, ProcessingParameterTemplate> temp = new TreeMap<>();
            temp.putAll(presetTemplate.getRunParameters());
            for (ProcessingParameterTemplate aRunParamter : temp.values()) {
                String parameterDescription = aRunParamter.getDescription();
                if (parameterDescription.isEmpty()) {
                    parameterDescription = "?";
                }
                tbModel.addRow(new Object[]{(tbModel.getRowCount() + 1), aRunParamter.getName(), aRunParamter.getValue(), parameterDescription, true});
            }
            temp.clear();
            temp.putAll(presetTemplate.getJobParameters());
            for (ProcessingParameterTemplate aJobParameter : temp.values()) {
                String parameterDescription = aJobParameter.getDescription();
                if (parameterDescription.isEmpty()) {
                    parameterDescription = "?";
                }
                tbModel.addRow(new Object[]{(tbModel.getRowCount() + 1), aJobParameter.getName(), aJobParameter.getValue(), parameterDescription, false});
            }
            //also do the prerequisites
            prerequisite = presetTemplate.getMachinePrerequisite();
            refreshPrerequisites();
            LOGGER.debug("Done...");
        }
        this.template = presetTemplate;
    }
    

    private void miImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miImportActionPerformed
        fileChooser.setCurrentDirectory(lastSelectedFolder);
        fileChooser.setDialogTitle("Select template file to import...");
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try {
                PladipusProcessingTemplate convertXMLtoTemplate = XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(fileToOpen);
                reloadTemplate(convertXMLtoTemplate);
            } catch (IOException | ProcessStepInitialisationException | ParserConfigurationException | SAXException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "An error occurred during importing : " + ex.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        refreshPreview();
    }//GEN-LAST:event_miImportActionPerformed
    
    private void refreshPrerequisites() {
        ArrayList<PrerequisiteParameter> prerequisiteList = prerequisite.getPrerequisiteList();
        boolean arch32 = false;
        boolean arch64 = false;
        boolean windows = false;
        boolean linux = false;
        for (PrerequisiteParameter aParameter : prerequisiteList) {
            switch (aParameter) {
                case ARCH_32:
                    arch32 = true;
                    break;
                case ARCH_64:
                    arch64 = true;
                    break;
                case OS_LINUX:
                    linux = true;
                    break;
                case OS_WINDOWS:
                    windows = true;
                    break;
                case CORES:
                    miSetCores.setText("Set minimal cores (" + aParameter.getOptionValue() + ")");
                    break;
                case MEMORY:
                    miSetMemory.setText("Set minimal RAM (" + aParameter.getOptionValue() + " GB)");
                    break;
                case DISKSPACE:
                    miSetDiskSpace.setText("Set minimal disk (" + aParameter.getOptionValue() + " GB)");
                    break;
                default:
                    break;
            }
        }
        if (arch64) {
            if (windows) {
                rdbWindows64.setEnabled(true);
            }
            if (linux) {
                rdbLinux64.setEnabled(true);
            }
        } else if (arch32) {
            if (windows) {
                rdbWindows32.setEnabled(true);
            }
            if (linux) {
                rdbLinux32.setEnabled(true);
            }
        }
    }
    

    private void rdbNoOsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbNoOsActionPerformed
        clearOSArchPrerequisites();
        refreshPreview();
    }//GEN-LAST:event_rdbNoOsActionPerformed
    
    private void swapElements(int pos1, int pos2) {
        if (pos1 != pos2) {
            DefaultListModel listModel = (DefaultListModel) liSteps.getModel();
            String tmp = String.valueOf(listModel.get(pos1));
            listModel.set(pos1, listModel.get(pos2));
            listModel.set(pos2, tmp);
        }
    }
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void cbStepsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cbStepsFocusLost

    }//GEN-LAST:event_cbStepsFocusLost

    private void cbPresetsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cbPresetsFocusLost

    }//GEN-LAST:event_cbPresetsFocusLost

    private void btnRemoveParameterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveParameterActionPerformed
        DefaultTableModel model = (DefaultTableModel) tblParameters.getModel();
        if (model != null) {
            for (int selectedRow : tblParameters.getSelectedRows()) {
                String parameterName = String.valueOf(model.getValueAt(selectedRow, 1));
                template.getJobParameters().remove(parameterName);
                template.getRunParameters().remove(parameterName);
                model.removeRow(selectedRow);
            }
            refreshPreview();
        }
    }//GEN-LAST:event_btnRemoveParameterActionPerformed

    private void btnAddParameterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddParameterActionPerformed
        DefaultTableModel model = (DefaultTableModel) tblParameters.getModel();
        if (model == null) {
            model = new DefaultTableModel();
            tblParameters.setModel(model);
        }
        model.addRow(new Object[]{(model.getRowCount() + 1), "", "", "", true});
    }//GEN-LAST:event_btnAddParameterActionPerformed

    private void cbPresetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPresetsActionPerformed
        
        if (cbPresets.getSelectedIndex() != -1) {
            
            cbSteps.setSelectedIndex(0);
            
            if (((String) cbPresets.getSelectedItem()).equalsIgnoreCase("Custom")) {
                cbSteps.setEnabled(true);
            } else {
                cbSteps.setEnabled(false);
            }
            
            if (cbPresets.getSelectedIndex() == 0) {
                
                DefaultListModel liModel = (DefaultListModel) liSteps.getModel();
                liModel.clear();
                
                DefaultTableModel tbModel = (DefaultTableModel) tblParameters.getModel();
                tbModel.setRowCount(0);
                
                try {
                    reloadTemplate(interpeter.convertXMLtoTemplate(presets.get("Custom")));
                } catch (ParserConfigurationException | SAXException | IOException ex) {
                    LOGGER.error(ex);
                }
                
            } else {
                try {
                    reloadTemplate(interpeter.convertXMLtoTemplate(presets.get(String.valueOf(cbPresets.getSelectedItem()))));
                } catch (ParserConfigurationException | SAXException | IOException ex) {
                    LOGGER.error(ex);
                }
            }
            
            refreshPreview();
        }
    }//GEN-LAST:event_cbPresetsActionPerformed

    private void cbStepsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbStepsActionPerformed
        
        if (cbSteps.getSelectedIndex() != 0) {
            
            if (template != null) {
                for (Object aSelectedStep : cbSteps.getSelectedObjects()) {
                    String step = String.valueOf(aSelectedStep);
                    String className = installedProcessStepClasses.get(step);
                    if (!template.getProcessingSteps().contains(className)) {
                        template.addProcessingStep(className);
                        ((DefaultListModel) liSteps.getModel()).addElement(step);
                    }
                }
            }
        }
        refreshPreview();
    }//GEN-LAST:event_cbStepsActionPerformed

    private void liStepsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_liStepsMouseReleased
        
        int row = liSteps.locationToIndex(evt.getPoint());
        
        if (row != -1) {
            liSteps.setSelectedIndex(row);
        }
        
        moveUpMenuItem.setEnabled(row > 0);
        moveDownMenuItem.setEnabled(row < liSteps.getModel().getSize() - 1);
        removeMenuItem.setEnabled(row != -1);
        
        stepsPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }//GEN-LAST:event_liStepsMouseReleased

    private void moveUpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpMenuItemActionPerformed
        int indexOfSelected = liSteps.getSelectedIndex();
        if (indexOfSelected > 0) {
            swapElements(indexOfSelected, indexOfSelected - 1);
            indexOfSelected = indexOfSelected - 1;
            liSteps.setSelectedIndex(indexOfSelected);
            liSteps.updateUI();
            refreshPreview();
        }
    }//GEN-LAST:event_moveUpMenuItemActionPerformed

    private void moveDownMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownMenuItemActionPerformed
        int indexOfSelected = liSteps.getSelectedIndex();
        DefaultListModel listModel = (DefaultListModel) liSteps.getModel();
        if (indexOfSelected < listModel.getSize() - 1) {
            swapElements(indexOfSelected, indexOfSelected + 1);
            indexOfSelected = indexOfSelected + 1;
            liSteps.setSelectedIndex(indexOfSelected);
            liSteps.updateUI();
            refreshPreview();
        }
    }//GEN-LAST:event_moveDownMenuItemActionPerformed

    private void removeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMenuItemActionPerformed
        for (Object aSelectedStep : liSteps.getSelectedValuesList()) {
            String step = String.valueOf(aSelectedStep);
            String className = installedProcessStepClasses.get(step);
            if (template.getProcessingSteps().contains(className)) {
                template.removeProcessingStep(className);
                ((DefaultListModel) liSteps.getModel()).removeElement(step);
            }
        }
        refreshPreview();
    }//GEN-LAST:event_removeMenuItemActionPerformed

    private void cbRetainJobOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbRetainJobOrderActionPerformed
        template.setKeepOrder(cbRetainJobOrder.isSelected());
    }//GEN-LAST:event_cbRetainJobOrderActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddParameter;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCreateRun;
    private javax.swing.ButtonGroup btnGroupOSArch;
    private javax.swing.JButton btnRemoveParameter;
    private javax.swing.JComboBox cbPresets;
    private javax.swing.JCheckBox cbRetainJobOrder;
    private javax.swing.JComboBox cbSteps;
    private javax.swing.JEditorPane epnlPreviewXML;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JLabel lbPreSet;
    private javax.swing.JLabel lbPreSet1;
    private javax.swing.JLabel lblParameterRemark;
    private javax.swing.JLabel lblPriority;
    private javax.swing.JList liSteps;
    private javax.swing.JMenuBar mbMain;
    private javax.swing.JMenuItem miClearPreferences;
    private javax.swing.JMenuItem miExit;
    private javax.swing.JMenu miFile;
    private javax.swing.JMenuItem miImport;
    private javax.swing.JMenu miPreferences;
    private javax.swing.JMenuItem miSetCores;
    private javax.swing.JMenuItem miSetDiskSpace;
    private javax.swing.JMenuItem miSetMemory;
    private javax.swing.JMenuItem moveDownMenuItem;
    private javax.swing.JMenuItem moveUpMenuItem;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlParameters;
    private javax.swing.JPanel pnlPreview;
    private javax.swing.JPanel pnlRunName;
    private javax.swing.JPanel pnlSteps;
    private javax.swing.JRadioButtonMenuItem rdbLinux32;
    private javax.swing.JRadioButtonMenuItem rdbLinux64;
    private javax.swing.JRadioButtonMenuItem rdbNoOs;
    private javax.swing.JRadioButtonMenuItem rdbWindows32;
    private javax.swing.JRadioButtonMenuItem rdbWindows64;
    private javax.swing.JMenuItem removeMenuItem;
    private javax.swing.JSlider slPriority;
    private javax.swing.JScrollPane spnlParameters;
    private javax.swing.JScrollPane spnlPreview;
    private javax.swing.JPopupMenu.Separator sprOS;
    private javax.swing.JPopupMenu stepsPopupMenu;
    private javax.swing.JTable tblParameters;
    private javax.swing.JTextField tfRunName;
    // End of variables declaration//GEN-END:variables

    public PladipusProcessingTemplate getProcessingTemplate() {
        return template;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ProcessImportDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProcessImportDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProcessImportDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProcessImportDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                try {
                    RunCreationDialog dialog;
                    dialog = new RunCreationDialog(new javax.swing.JFrame(), "pladmin", new UserPanel(), true);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            System.exit(0);
                        }
                    });
                    dialog.setVisible(true);
                } catch (ParserConfigurationException ex) {
                    java.util.logging.Logger.getLogger(RunCreationDialog.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(RunCreationDialog.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    java.util.logging.Logger.getLogger(RunCreationDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });
    }
    
}
