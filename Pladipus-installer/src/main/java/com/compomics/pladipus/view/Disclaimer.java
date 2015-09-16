package com.compomics.pladipus.view;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class Disclaimer extends javax.swing.JDialog {

    /**
     * Creates a new Disclaimer.
     */
    public Disclaimer(java.awt.Frame parent, boolean modal) throws IOException {
        super(parent, modal);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initComponents();
        setTitle("Pladipus Disclaimer");

        ImageIcon image = new ImageIcon(
                getClass().getResource(
                        "/images/Compomics_logo.jpg"));
        lbLogo.setText("");
        lbLogo.setIcon(image);

        epDisclaimer.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        epDisclaimer.setEditable(false);
        epDisclaimer.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (IOException | URISyntaxException ex) {
                            Logger.getLogger(InstallerGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/pladipus_icon.gif")));
        readDisclaimer();
        this.setLocationRelativeTo(parent);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMain = new javax.swing.JPanel();
        btnInstall = new javax.swing.JButton();
        spnlDisclaimer = new javax.swing.JScrollPane();
        epDisclaimer = new javax.swing.JEditorPane();
        lbLogo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pnlMain.setBackground(new java.awt.Color(255, 255, 255));

        btnInstall.setText("Install");
        btnInstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInstallActionPerformed(evt);
            }
        });

        spnlDisclaimer.setBackground(new java.awt.Color(255, 255, 255));
        spnlDisclaimer.setBorder(null);

        epDisclaimer.setEditable(false);
        epDisclaimer.setBorder(null);
        spnlDisclaimer.setViewportView(epDisclaimer);

        lbLogo.setText("lbLogo");

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lbLogo)
                    .addComponent(spnlDisclaimer, javax.swing.GroupLayout.PREFERRED_SIZE, 641, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(921, 921, 921)
                .addComponent(btnInstall)
                .addGap(10, 10, 10))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(lbLogo)
                .addGap(18, 18, 18)
                .addComponent(spnlDisclaimer, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnInstall)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnInstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInstallActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnInstallActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    private void readDisclaimer() throws IOException {
        epDisclaimer.setContentType("text/html");//set content as html
        File tempFile = File.createTempFile("disclaimer", ".html");
        tempFile.deleteOnExit();
        try (OutputStream out = new FileOutputStream(tempFile); InputStream in = getClass().getClassLoader().getResource("doc/disclaimer.html").openStream()) {
            IOUtils.copy(in, out);
        }
        epDisclaimer.setPage(tempFile.toURI().toURL());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnInstall;
    private javax.swing.JEditorPane epDisclaimer;
    private javax.swing.JLabel lbLogo;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JScrollPane spnlDisclaimer;
    // End of variables declaration//GEN-END:variables
}
