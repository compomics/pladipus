package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.model.enums.AllowedPeptideShakerReportParams;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import com.compomics.pladipus.search.checkpoints.PeptideShakerReportCheckPoints;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author Kenneth Verheggen
 */
public class PeptideShakerReportStep extends PeptideShakerStep {

    public PeptideShakerReportStep() {

    }

    private List<String> constructArguments() throws IOException, XMLStreamException, URISyntaxException, UnspecifiedPladipusException {
        File peptideShakerJar = getJar();

        ArrayList<String> cmdArgs = new ArrayList<>();
        cmdArgs.add("java");
        cmdArgs.add("-cp");
        cmdArgs.add(peptideShakerJar.getAbsolutePath());
        cmdArgs.add("eu.isas.peptideshaker.cmd.ReportCLI");
        //if there are no specific reports required
        if (!parameters.containsKey(AllowedPeptideShakerReportParams.REPORT_TYPE.getId())) {
        /*    parameters.put(AllowedPeptideShakerReportParams.REPORT_TYPE.getId(),
                    "0,1,2,3,4");
        */
            throw new IllegalArgumentException("did not pass an allowed report type");
        }
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
    public boolean doAction() throws PladipusProcessingException, UnspecifiedPladipusException {
        try {
            List<String> constructArguments = constructArguments();
            File peptideShakerJar = getJar();
            //add callback notifier for more detailed printouts of the processing
            CallbackNotifier callbackNotifier = getCallbackNotifier();
            for (PeptideShakerReportCheckPoints aCheckPoint : PeptideShakerReportCheckPoints.values()) {
                callbackNotifier.addCheckpoint(new Checkpoint(aCheckPoint.getLine(), aCheckPoint.getFeedback()));
            }
            startProcess(peptideShakerJar, constructArguments);
            return true;
        } catch (IOException | XMLStreamException | URISyntaxException ex) {
            throw new PladipusProcessingException(ex);
        }
    }

    @Override
    public String getDescription() {
        return "Running PeptideShaker Report CLI";
    }
}
