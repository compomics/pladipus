package com.compomics.pladipus.moff.logic.step;

import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.search.checkpoints.PeptideShakerReportCheckPoints;
import com.compomics.pladipus.search.processsteps.PeptideShakerStep;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamException;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 * @author Niels Hulstaert
 */
public class MoFFPeptideShakerConversionStep extends PeptideShakerStep {

    /**
     * Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(MoFFPeptideShakerConversionStep.class);

    private static final String REPORT_SUFFIX = "Default_PSM_Report.txt";

    /**
     * The report file.
     */
    private File reportFile;
    private File fastaFile;
    private File mgfFile;

    public MoFFPeptideShakerConversionStep() {

    }

    private void createReportFile(File cpsxFile) throws IOException, XMLStreamException, URISyntaxException, UnspecifiedPladipusException {
        //check if the FASTA and MGF file are in the same directory as the cpsx file
        File cpsxParentDirectory = cpsxFile.getParentFile();
        File originalFastaFile = new File(parameters.get("fasta"));
        if(!originalFastaFile.getParentFile().getAbsolutePath().equals(cpsxParentDirectory.getAbsolutePath())){
            //copy it to the same directory as the cpsx file
            fastaFile = new File(cpsxParentDirectory, originalFastaFile.getName());
            Files.copy(originalFastaFile, fastaFile);
        }
        File originalMgfFile = new File(parameters.get("mgf"));
        if(!originalMgfFile.getParentFile().getAbsolutePath().equals(cpsxParentDirectory.getAbsolutePath())){
            //copy it to the same directory as the cpsx file
            mgfFile = new File(cpsxParentDirectory, originalMgfFile.getName());
            Files.copy(originalMgfFile, mgfFile);
        }
                
        File peptideShakerJar = getJar();

        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideShakerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.ReportCLI");
        cmdArgs.add("-in");
        cmdArgs.add(cpsxFile.getAbsolutePath());
        cmdArgs.add("-out_reports");
        cmdArgs.add(cpsxFile.getParentFile().getAbsolutePath());
        cmdArgs.add("-reports");
        cmdArgs.add("3");

        //add callback notifier for more detailed printouts of the processing
        CallbackNotifier callbackNotifier = getCallbackNotifier();
        for (PeptideShakerReportCheckPoints aCheckPoint : PeptideShakerReportCheckPoints.values()) {
            callbackNotifier.addCheckpoint(new Checkpoint(aCheckPoint.getLine(), aCheckPoint.getFeedback()));
        }
        new ProcessingEngine().startProcess(peptideShakerJar, cmdArgs, callbackNotifier);
    }

    @Override
    public boolean doAction() throws UnspecifiedPladipusException, PladipusProcessingException {
        try {
            File cpsxFile = new File(parameters.get("ps_output"));

            createReportFile(cpsxFile);

            //remove the temporary FASTA and MGF files and inedex files if necessary
            if(fastaFile != null){
                File fastaIndexFile = new File(fastaFile.getAbsoluteFile() + ".cui");
                fastaIndexFile.delete();
                fastaFile.delete();                
            }
            if(mgfFile != null){
                File mgfIndexFile = new File(mgfFile.getAbsoluteFile() + ".cui");
                mgfIndexFile.delete();
                mgfFile.delete();
            }
                        
            File[] files = cpsxFile.getParentFile().listFiles((File dir, String name) -> name.endsWith(REPORT_SUFFIX));
            if (files.length != 0) {
                //get the first file and rename it
                reportFile = new File(cpsxFile.getParentFile(), cpsxFile.getName() + ".report.tsv");
                Files.move(files[0], reportFile);
            } else {
                throw new FileNotFoundException("The report file was not found.");
            }            

            LOGGER.info("Conversion completed");
            return true;
        } catch (IOException | XMLStreamException | URISyntaxException ex) {
            throw new PladipusProcessingException(ex);
        }
    }

    @Override
    public String getDescription() {
        return "Converting PeptideShaker output to MoFF format";
    }

    public File getMoffFile() {
        return reportFile;
    }

    public static void main(String[] args) {
        ProcessingStep.main(args);
    }
}
