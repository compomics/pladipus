package com.compomics.pladipus.msconvert;

import com.compomics.pladipus.core.model.enums.AllowedMsConvertParams;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
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
    private final File tempResults = new File(System.getProperty("user.home") + "/pladipus/temp/MsConvert/results");
    private File executable;
    private File real_outputFolder;

    public MsConvertStep() {

    }

    private List<String> constructArguments() throws IOException {
        ArrayList<String> cmdArgs = new ArrayList<>();
        executable = new File(parameters.get("pwiz_folder") + "/msconvert");
        cmdArgs.add(parameters.get("pwiz_folder") + "/msconvert");
        for (AllowedMsConvertParams aParameter : AllowedMsConvertParams.values()) {
            if (parameters.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                String value = parameters.get(aParameter.getId());
                if (!value.isEmpty()) {
                    cmdArgs.add(value);
                }
            } else if (aParameter.isMandatory()) {
                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
            }
        }
        //cs filters?
        if (parameters.containsKey("-filter")) {
            String[] filters = parameters.get("-filter").split(";");
            for (String aFilter : filters) {
                cmdArgs.add("--filter");
                cmdArgs.add('"' + aFilter + '"');
            }
        }
        return cmdArgs;
    }

    public void setOutputFolder(File outputFolder) {
        this.real_outputFolder = outputFolder;
    }

    @Override
    public boolean doAction() throws PladipusProcessingException {
        System.out.println("Running " + this.getClass().getName());
        boolean success = false;
        if (real_outputFolder == null) {
            real_outputFolder = new File(parameters.get("o"));
        }
        try {
            if (checkOS()) {
                //convert the RAW file
                parameters.put("o", tempResults.getAbsolutePath());
                constructArguments();

                startProcess(executable, constructArguments());

                File[] resultFiles = tempResults.listFiles();

                for (File aResultFile : resultFiles) {
                    //deliver the file to the correct location
                    File output = new File(real_outputFolder, aResultFile.getName());
                    copyFile(aResultFile, output);
                }
            }
            //deleteFolder(tempResults);
            LOGGER.info("DONE");
            success = true;
        } catch (Exception e) {
            throw new PladipusProcessingException(e);
        }
        return success;
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

    public static void main(String[] args) {
        ProcessingStep.main(args);
    }
}
