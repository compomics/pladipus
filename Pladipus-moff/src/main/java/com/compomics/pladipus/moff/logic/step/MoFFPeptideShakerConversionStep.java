package com.compomics.pladipus.moff.logic.step;

import com.compomics.moff.gui.control.util.PSOutputParser;
import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import com.compomics.pladipus.search.checkpoints.PeptideShakerReportCheckPoints;
import com.compomics.pladipus.search.processsteps.PeptideShakerStep;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class MoFFPeptideShakerConversionStep extends PeptideShakerStep {

    /**
     * The input file (can be either a peptideshaker extended report, a CPSX
     * file or a zipped export made through the GUI)
     */
    private File inputFile;
    /**
     * The output folder (where the moff compatible file will be written to)
     */
    private File outputFolder;
    /**
     * The output file
     */
    private File moffFile;
    /**
     * The hypothetical data folder that peptideshaker uses to find the FASTA
     * and MGF files
     */
    private File dataFolder;
    /**
     * boolean indicating if the datafolder was used as a temp folder or if it
     * was already there
     */
    private boolean deleteDataFolder;
    /**
     * The temporary mgf in processing
     */
    private File tempMgf;
    /**
     * boolean indicating if the temp mgf can be deleted
     */
    private boolean deleteTempMGF;
    /**
     * Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(MoFFPeptideShakerConversionStep.class);

    public MoFFPeptideShakerConversionStep() {

    }

    private File prepareReportFile(File inputFile) throws IOException, XMLStreamException, URISyntaxException {
        File reportFile = inputFile;
        this.outputFolder = new File(inputFile.getParentFile().getAbsolutePath() + "/reports");
        File tempFolder = new File(outputFolder, "temp" + System.currentTimeMillis());
        tempFolder.mkdirs();
        if (inputFile.getName().toUpperCase().endsWith(".TSV") || inputFile.getName().toUpperCase().endsWith(".TXT")) {
            reportFile = inputFile;
        } else {
            File inputFolder = inputFile.getParentFile();
            dataFolder = new File(inputFolder, "data");
            deleteDataFolder = !dataFolder.exists();
            if (inputFile.getName().toUpperCase().endsWith(".CPSX")) {
                //check if the fasta and MGF are present...if not, get them from the parameters and copy them over to the temp folder?
                dataFolder.mkdirs();
                //first check if the mgf and fasta are specified in parameters AND if they exist?
                if (parameters.containsKey("fasta")) {
                    File fastaFile = new File(parameters.get("fasta"));
                    File tempFasta = new File(dataFolder, fastaFile.getName());
                    if (!tempFasta.exists()) {
                        FileUtils.copyFile(fastaFile, tempFasta);
                    }
                }
                if (parameters.containsKey("mgf")) {
                    File mgfFile = new File(parameters.get("mgf"));
                    tempMgf = new File(dataFolder, mgfFile.getName());
                    if (!tempMgf.exists()) {
                        FileUtils.copyFile(mgfFile, tempMgf);
                    } else {
                        deleteTempMGF = true;
                    }
                }
            }
            //if they don't exist, the user should have put them in the input folder. PeptideShaker will handle this?
        }
        File peptideShakerJar = getJar();

        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideShakerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.ReportCLI");
        cmdArgs.add("-in");
        cmdArgs.add(inputFile.getAbsolutePath());
        cmdArgs.add("-out_reports");
        cmdArgs.add(tempFolder.getAbsolutePath());
        cmdArgs.add("-reports");
        cmdArgs.add("8");

        //add callback notifier for more detailed printouts of the processing
        CallbackNotifier callbackNotifier = getCallbackNotifier();
        for (PeptideShakerReportCheckPoints aCheckPoint : PeptideShakerReportCheckPoints.values()) {
            callbackNotifier.addCheckpoint(new Checkpoint(aCheckPoint.getLine(), aCheckPoint.getFeedback()));
        }
        new ProcessingEngine().startProcess(peptideShakerJar, cmdArgs, callbackNotifier);
        //get the resulting file and clean up
        File[] listFiles = tempFolder.listFiles();
        if (listFiles.length == 0) {
            throw new FileNotFoundException("Result file was not created.");
        } else {
            File temp = listFiles[0];
            reportFile = new File(inputFile.getAbsolutePath() + ".report.tsv");
            temp.renameTo(reportFile);
            return reportFile;
        }
    }

    @Override
    public boolean doAction() throws Exception {
        inputFile = new File(parameters.get("ps_output"));
        File reportFile = prepareReportFile(inputFile);
        moffFile = new File(reportFile.getAbsolutePath() + ".moff.tsv");
        PSOutputParser.convertReport(reportFile, moffFile);
        clearDataFolder();
        LOGGER.info("Conversion completed");
        return true;
    }

    @Override
    public String getDescription() {
        return "Converting PeptideShaker output to MoFF format";
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getMoffFile() {
        return moffFile;
    }

    public void clearDataFolder() throws IOException {
        if (dataFolder != null && dataFolder.exists()) {
            if (deleteDataFolder) {
                FileUtils.deleteDirectory(dataFolder);
            } else if (deleteTempMGF) {
                tempMgf.delete();
            }
        }
    }
}
