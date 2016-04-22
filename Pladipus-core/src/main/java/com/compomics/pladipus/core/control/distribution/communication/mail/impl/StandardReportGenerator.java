package com.compomics.pladipus.core.control.distribution.communication.mail.impl;

import com.compomics.pladipus.core.control.distribution.communication.mail.ReportGenerator;
import com.compomics.pladipus.core.control.distribution.service.RunService;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.prerequisite.PrerequisiteParameter;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import java.io.IOException;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class StandardReportGenerator implements ReportGenerator {
/**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(StandardReportGenerator.class);
    
    
    @Override
    public String generateReport(int runID) throws SQLException, IOException {
        StringBuilder message = new StringBuilder();
        try {
            RunService rService = RunService.getInstance();
            String runTitle = rService.getRunTitle(runID);
            PladipusProcessingTemplate templateForRun = rService.getTemplateForRun(runID);
            message.append("Good news : your Pladipus run was completed !").append("<br>");
            message.append("=======").append("<br>");
            message.append("<h1>Summary</h1>").append("<br>");
            message.append("=======").append("<br>");
            message.append("ID\t:").append(runID).append("<br>");
            message.append("Title\t:").append(runTitle);
            message.append("Priority\t:").append(templateForRun.getPriority()).append("<br>");
            message.append("Prerequisites").append("<br>");
            for (PrerequisiteParameter aPrerequisite : templateForRun.getMachinePrerequisite().getPrerequisiteList()) {
                message.append("Priority\t").append(aPrerequisite.getSystemParameterName()).append("\t:").append(aPrerequisite.getOptionValue()).append("<br>");
            }
            message.append("Classes to run").append("<br>");
            int stepCounter = 1;
            for (String aStep : templateForRun.getProcessingSteps()) {
                message.append(stepCounter).append(".").append(aStep).append("<br>");
                stepCounter++;
            }
            
        } catch (ProcessStepInitialisationException | ParserConfigurationException | SAXException ex) {
            LOGGER.error(ex);
        }
        return message.toString();
    }

    @Override
    public String generateSubject(int runID) throws SQLException, IOException {
        return "Your pladipus run was completed.";
    }
}
