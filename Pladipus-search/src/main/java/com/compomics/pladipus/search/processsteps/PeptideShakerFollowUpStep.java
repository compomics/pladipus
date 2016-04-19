package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.enums.AllowedPeptideShakerFollowUpParams;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import com.compomics.pladipus.search.checkpoints.PeptideShakerReportCheckPoints;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerFollowUpStep extends PeptideShakerStep {

    private File real_output_folder;
    private static final File temp_peptideshaker_output = new File(System.getProperty("user.home") + "/pladipus/temp/search/PeptideShaker/mgf");

    public PeptideShakerFollowUpStep() {

    }

    private List<String> constructArguments() throws IOException, XMLStreamException, URISyntaxException {
        if (temp_peptideshaker_output.exists()) {
            FileUtils.deleteDirectory(temp_peptideshaker_output);
        }
        temp_peptideshaker_output.mkdirs();
        real_output_folder = new File(parameters.get("output_folder"));
        File peptideShakerJar = getJar();
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideShakerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.FollowUpCLI");
        //construct the cmd
        for (AllowedPeptideShakerFollowUpParams aParameter : AllowedPeptideShakerFollowUpParams.values()) {
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
        List<String> constructArguments = constructArguments();
        File peptideShakerJar = getJar();
        //add callback notifier for more detailed printouts of the processing
        CallbackNotifier callbackNotifier = getCallbackNotifier();
        for (PeptideShakerReportCheckPoints aCheckPoint : PeptideShakerReportCheckPoints.values()) {
            callbackNotifier.addCheckpoint(new Checkpoint(aCheckPoint.getLine(), aCheckPoint.getFeedback()));
        }
        startProcess(peptideShakerJar, constructArguments);
        //run peptideShaker with the existing files
        cleanupAndSave();
        return true;
    }

    private void cleanupAndSave() throws IOException {
        //parameters.put("out",real_output_file.getAbsolutePath());
        //copy as a stream?
        if (!real_output_folder.exists()) {
            real_output_folder.mkdirs();
        }
        for (File anMGF : temp_peptideshaker_output.listFiles()) {
            File zipMGF = new File(anMGF.getAbsolutePath() + ".zip");
            ZipUtils.zipFile(anMGF, zipMGF);
            File real_output_file = new File(real_output_folder, zipMGF.getName());
            real_output_file.createNewFile();
            System.out.println("Copying " + zipMGF.getAbsolutePath() + " to " + real_output_file.getAbsolutePath());
            try (FileChannel source = new FileInputStream(zipMGF).getChannel();
                    FileChannel destination = new FileOutputStream(real_output_file).getChannel()) {
                destination.transferFrom(source, 0, source.size());
            }
            //delete the local one
            anMGF.delete();
            zipMGF.delete();
        }
    }

    @Override
    public String getDescription() {
        return "Running PeptideShaker FollowUpCLI";
    }
}
