/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.msconvert;

import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.enums.AllowedMsConvertParams;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class MsConvertStep extends ProcessingStep {

    private static final Logger LOGGER = Logger.getLogger(MsConvertStep.class);
    private final File tempResults = new File(System.getProperty("user.home") + "/.compomics/pladipus/temp/MsConvert/results");

    public MsConvertStep() {

    }

    private List<String> constructArguments() throws IOException {
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add(parameters.get("pwiz_folder") + "/msconvert");
        for (AllowedMsConvertParams aParameter : AllowedMsConvertParams.values()) {
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
    public boolean doAction() throws Exception {
        System.out.println("Running " + this.getClass().getName());
        boolean success = false;
        try {
            if (checkOS()) {
                File input = new File("f");
                File tempMGF = new File(input.getParentFile(), "temp_" + System.currentTimeMillis());
                //convert the RAW file
                File real_outputFolder = new File(parameters.get("o"));
                parameters.put("o", tempResults.getAbsolutePath());
                constructArguments();
                File[] resultFiles = tempMGF.listFiles();

                LOGGER.info("Processing complete...Zipping result MGF...");
                for (File aResultFile : resultFiles) {
                    String fileName = aResultFile.getName().substring(0, aResultFile.getName().indexOf(".")) + ".zip";
                    File zippedOutput = new File(aResultFile.getParentFile(), fileName);
                    ZipUtils.zipFile(aResultFile, zippedOutput);
                    //deliver the file to the correct location
                    File output = new File(real_outputFolder, zippedOutput.getName());
                    if (!output.getName().toLowerCase().endsWith(".zip")) {
                        output = new File(output.getAbsolutePath() + ".zip");
                    }
                    copyFile(zippedOutput, output);
                }
            }
            LOGGER.info("DONE");
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            deleteFolder(tempResults.getParentFile());
            return success;
        }
    }

    @Override
    public String getDescription() {
        return "Running MSConvert";
    }

    private boolean checkOS() {
        String currentOS = System.getProperty("os.name").toLowerCase();
        if (currentOS.contains("win")) {
            return true;
        }
        throw new UnsupportedOperationException("MsConvert uses vendor specific libraries only available on windows !");
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        LOGGER.info("Copying " + sourceFile.getAbsolutePath() + " to " + destFile);
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
                FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }

    
   
    
    private boolean deleteFolder(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    System.out.println("Deleted " + f.getAbsolutePath());
                    f.delete();
                }
            }
        }

        return path.delete();
    }

}
