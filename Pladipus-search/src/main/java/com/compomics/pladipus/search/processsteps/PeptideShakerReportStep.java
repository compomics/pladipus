/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.util.JarLookupService;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.enums.AllowedPeptideShakerReportParams;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.search.checkpoints.PeptideShakerReportCheckPoints;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerReportStep extends ProcessingStep {

    private File real_output_folder;
    private File temp;

    public PeptideShakerReportStep() {

    }

    private List<String> constructArguments() throws IOException {
        File peptideShakerJar = getJar();
        ArrayList<String> cmdArgs = new ArrayList<>();
        temp = new File(System.getProperty("user.home") + "/compomics/.compomics/pladipus/temp/PeptideShaker/reports");
        temp.mkdirs();
        cmdArgs.add("java");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideShakerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.ReportCLI");
        //if this is part of the peptideshaker pipeline
        if (parameters.containsKey("out")) {
            //the output of the cps
            parameters.put("in", parameters.get("out"));
        }
        if (!parameters.containsKey(AllowedPeptideShakerReportParams.EXPORT_FOLDER.getId())) {
            real_output_folder = new File(parameters.get("out")).getParentFile();
        } else {
            real_output_folder = new File(parameters.get(AllowedPeptideShakerReportParams.EXPORT_FOLDER.getId()));
        }
        parameters.put(AllowedPeptideShakerReportParams.EXPORT_FOLDER.getId(), temp.getAbsolutePath());

        //if there are no specific reports required
        if (!parameters.containsKey(AllowedPeptideShakerReportParams.REPORT_TYPE.getId())) {
            parameters.put(AllowedPeptideShakerReportParams.REPORT_TYPE.getId(),
                    "0,1,2,3,4");
        }
        //construct the cmd
        for (AllowedPeptideShakerReportParams aParameter : AllowedPeptideShakerReportParams.values()) {
            if (parameters.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(parameters.get(aParameter.getId()));
            } else if (aParameter.isMandatory()) {
                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
            }
        }
        return cmdArgs;
    }

    @Override
    public boolean doAction() throws Exception, Exception {
        System.out.println("Running " + this.getClass().getName());
        List<String> constructArguments = constructArguments();
        File peptideShakerJar = getJar();
        if (temp.exists()) {
            FileUtils.deleteDirectory(temp);
        }
        temp.mkdirs();

        //add callback notifier for more detailed printouts of the processing
        CallbackNotifier callbackNotifier = getCallbackNotifier();
        for (PeptideShakerReportCheckPoints aCheckPoint : PeptideShakerReportCheckPoints.values()) {
            callbackNotifier.addCheckpoint(new Checkpoint(aCheckPoint.getLine(), aCheckPoint.getFeedback()));
        }
        new ProcessingEngine().startProcess(peptideShakerJar, constructArguments, callbackNotifier);
        //run peptideShaker with the existing files
        cleanupAndSave();
        return true;
    }

    public File getJar() throws IOException {
        //check if this is possible in another way...
        File toolFolder = new File(System.getProperties().getProperty("user.home") + "/.compomics/pladipus/tools");
        toolFolder.mkdirs();
        //check if searchGUI already exists?
        File temp = new File(toolFolder, "PeptideShaker");
        if (!temp.exists()) {
            File peptideshakerFile = PladipusFileDownloadingService.downloadFile(parameters.get("PeptideShaker"), toolFolder);
            if (peptideshakerFile.getName().endsWith(".zip")) {
                ZipUtils.unzipArchive(peptideshakerFile, temp);
            }
        }
        return JarLookupService.lookupFile("PeptideShaker-.*.jar", temp);
    }

    private void cleanupAndSave() throws IOException {
        //parameters.put("out",real_output_file.getAbsolutePath());
        File temp = new File(parameters.get(AllowedPeptideShakerReportParams.EXPORT_FOLDER.getId()));
        //copy as a stream?
        if (!real_output_folder.exists()) {
            real_output_folder.createNewFile();
        }
        for (File aFile : temp.listFiles()) {
            File real_output_file = new File(real_output_folder, aFile.getName());
            if (real_output_file.exists()) {
                real_output_file.delete();
            }
            real_output_file.getParentFile().mkdirs();
            real_output_file.createNewFile();
            System.out.println("Copying " + aFile.getAbsolutePath() + " to " + real_output_file.getAbsolutePath());
            try (FileChannel source = new FileInputStream(aFile).getChannel();
                    FileChannel destination = new FileOutputStream(real_output_file).getChannel()) {
                destination.transferFrom(source, 0, source.size());
            }
        }
        //check if reports should be made
        if (!parameters.containsKey("generate_reports")) {
            FileUtils.deleteDirectory(temp);
        }
    }

    @Override
    public String getDescription() {
        return "Running PeptideShaker Report CLI";
    }
}
