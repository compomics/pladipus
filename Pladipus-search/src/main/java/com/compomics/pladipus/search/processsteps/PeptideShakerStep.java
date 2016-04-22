package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.control.runtime.diagnostics.memory.MemoryWarningSystem;
import com.compomics.pladipus.core.control.util.JarLookupService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.enums.AllowedPeptideShakerFollowUpParams;
import com.compomics.pladipus.core.model.enums.AllowedPeptideShakerParams;
import com.compomics.pladipus.core.model.enums.AllowedPeptideShakerReportParams;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.search.checkpoints.PeptideShakerCheckPoints;
import static com.compomics.software.autoupdater.DownloadLatestZipFromRepo.downloadLatestZipFromRepo;
import com.compomics.software.autoupdater.HeadlessFileDAO;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerStep extends ProcessingStep {

    private static final File temp_peptideshaker_output = new File(System.getProperty("user.home") + "/pladipus/temp/search/PeptideShaker");

    private static final Logger LOGGER = Logger.getLogger(PeptideShakerStep.class);
    private File temp_peptideshaker_cps;

    public PeptideShakerStep() {

    }

    private List<String> constructArguments() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedPladipusException {
        File peptideShakerJar = getJar();
        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-Xmx" + MemoryWarningSystem.getAllowedRam() + "M");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideShakerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.PeptideShakerCLI");
        //check if folder exists      
        File outputFolder = new File(parameters.get("output_folder"));
        outputFolder.mkdirs();
//check if reports are requested
        if (parameters.containsKey("reports") && !parameters.containsKey("out_reports")) {
            File outputReportFolder = new File(outputFolder, "reports");
            if (outputReportFolder.mkdirs()) {
                parameters.put("out_reports", outputReportFolder.getAbsolutePath());
            }
        }

        for (AllowedPeptideShakerParams aParameter : AllowedPeptideShakerParams.values()) {
            if (parameters.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(parameters.get(aParameter.getId()));
            } else if (aParameter.isMandatory()) {
                throw new IllegalArgumentException("Missing mandatory parameter : " + aParameter.id);
            }
        }
        //also add these for other possible CLI's?
        for (AllowedPeptideShakerFollowUpParams aParameter : AllowedPeptideShakerFollowUpParams.values()) {
            if (parameters.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(parameters.get(aParameter.getId()));
            }
        }
        for (AllowedPeptideShakerReportParams aParameter : AllowedPeptideShakerReportParams.values()) {
            if (parameters.containsKey(aParameter.getId())) {
                cmdArgs.add("-" + aParameter.getId());
                cmdArgs.add(parameters.get(aParameter.getId()));
            }
        }

        return cmdArgs;
    }

    @Override
    public boolean doAction() throws PladipusProcessingException, UnspecifiedPladipusException {
        try {
            LOGGER.info("Running Peptide Shaker");
            File peptideShakerJar = getJar();
            File realOutput = new File(parameters.get("output_folder"));
            File temporaryOutput = new File(temp_peptideshaker_output, realOutput.getName());
            if (temp_peptideshaker_output.exists()) {
                for (File aFile : temp_peptideshaker_output.listFiles()) {
                    try {
                        if (!aFile.isDirectory()) {
                            aFile.delete();
                        } else {
                            FileUtils.deleteDirectory(aFile);
                        }
                    } catch (Exception e) {
                        LOGGER.warn(e);
                    }
                }
            }
            temporaryOutput.mkdirs();
            String experiment = "output";
            
            if (parameters.containsKey("experiment")) {
                experiment = parameters.get("experiment");
            }
            
            String sample = "respin";
            if (parameters.containsKey("sample")) {
                sample = parameters.get("sample");
            }
            
            String replicate = "0";
            if (parameters.containsKey("replicate")) {
                replicate = parameters.get("replicate");
            }
            
            if (parameters.containsKey("output_folder")) {
                temp_peptideshaker_cps = new File(temp_peptideshaker_output.getAbsolutePath() + "/" + experiment + "_" + sample + "_" + replicate + ".cpsx");
                parameters.put("out", temp_peptideshaker_cps.getAbsolutePath());
            }
            File real_output_folder = new File(parameters.get("output_folder"));
            
            List<String> constructArguments = constructArguments();
            //add callback notifier for more detailed printouts of the processing
            CallbackNotifier callbackNotifier = getCallbackNotifier();
            for (PeptideShakerCheckPoints aCheckPoint : PeptideShakerCheckPoints.values()) {
                callbackNotifier.addCheckpoint(new Checkpoint(aCheckPoint.getLine(), aCheckPoint.getFeedback()));
            }
            startProcess(peptideShakerJar, constructArguments);
            cleanupAndSave(real_output_folder);
            return true;
        } catch (IOException | XMLStreamException | URISyntaxException ex) {
           throw new PladipusProcessingException(ex);
        } catch (Exception ex) {
             throw new UnspecifiedPladipusException(ex);
        }
    }

    public File getJar() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedPladipusException {
        File temp = new File(parameters.get("ps_folder"));
        if (!temp.exists()) {
            LOGGER.info("Downloading latest PeptideShaker version...");
            URL jarRepository = new URL("http", "genesis.ugent.be", new StringBuilder().append("/maven2/").toString());
            downloadLatestZipFromRepo(temp, "PeptideShaker", "eu.isas.peptideshaker", "PeptideShaker", null, null, jarRepository, false, false, new HeadlessFileDAO(), new WaitingHandlerCLIImpl());
        }
        return JarLookupService.lookupFile("PeptideShaker-.*.jar", temp);
    }

    public boolean aVersionExistsLocal() {
        //TODO insert installer code here in case PeptideShaker was not included????
        return true;
    }

    private void cleanupAndSave(File resultFolder) throws Exception {
        ZipUtils.zipFolder(temp_peptideshaker_output, new File(resultFolder, resultFolder.getName() + ".zip"));
    }

    @Override
    public String getDescription() {
        return "Running PeptideShaker";
    }
}
