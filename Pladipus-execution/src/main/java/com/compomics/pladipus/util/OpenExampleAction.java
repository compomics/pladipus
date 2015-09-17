/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.util;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.view.panels.impl.UserPanel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth
 */
public class OpenExampleAction {

    public void openExample(String loggedInUser,UserPanel userPanel) {
        try {
            //Custom button text
            Object[] options = {"Run Distributed",
                "Run Locally",};
            int n = JOptionPane.showOptionDialog(null,
                    "This option will load an example Run. A result folder needs to be specified. " + System.lineSeparator()
                    + "It is crucial that this folder is correct and can be reached by the entire network!",
                    "Open Example",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            String newOutputParentPath = "";
            if (n > -1) {
                //load the template from the user_file to set the output folders?
                PladipusProcessingTemplate template = getTemplateFromResource();
                template.setUser(loggedInUser);
                template.setName("Example Run (" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp(System.currentTimeMillis())) + ")");
                if (n == 1) {
                    JFileChooser fc = new JFileChooser();
                    fc.setMultiSelectionEnabled(false);
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fc.setCurrentDirectory(new File(System.getProperty("user.home")));
                    int returnVal = fc.showOpenDialog(userPanel);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        newOutputParentPath = fc.getSelectedFile().getAbsolutePath();

                    } else {
                        return;
                    }
                } else {
                    newOutputParentPath = JOptionPane.showInputDialog(null, "Please provide the (network) path that can be reached from all workers.");
                }
                if (newOutputParentPath != null && !newOutputParentPath.isEmpty()) {
                    File newConfig = overrideOutputFolder(newOutputParentPath);
                    new UploadRunAction().executeUpload(template, newConfig, userPanel);
                    JOptionPane.showMessageDialog(null,
                            "The Run was succesfully posted. To start the Run, select the Run in the upper panel, right click and \"Start Run\"",
                            "Run Created", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PladipusProcessingTemplate getTemplateFromResource() throws IOException, ParserConfigurationException, StepLoadingException, SAXException {
        try (StringWriter writer = new StringWriter();
                InputStream inputStream = getClass().getClassLoader().getResource("example/example_template.xml").openStream()) {
            IOUtils.copy(inputStream, writer);
            return XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(writer.toString());
        }
    }

    private File overrideOutputFolder(String outputFolderPath) throws IOException {
        File temp = File.createTempFile("example_", "temp.tsv");
        try (FileWriter writer = new FileWriter(temp); BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResource("example/example_configuration.tsv").openStream()))) {
            String line;
            //write headers
            writer.append(br.readLine()).append(System.lineSeparator()).flush();
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\t");
                String outputAddress = outputFolderPath + "/" + new File(split[4]).getName();
                writer.append(line.replace(split[4], outputAddress)).append(System.lineSeparator()).flush();
            }
        }
        return temp;
    }

}
