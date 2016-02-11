package com.compomics.pladipus.standalone;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.XMLInterpreter;
import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 *
 * @author compomics
 */
public class Launcher {

    private static Options options;
    private static final Logger LOGGER = Logger.getLogger(Launcher.class);
    private File template;
    private File config;
    private String runName;

    private static void constructOptions() {
        //options
        options = new Options();
        options.addOption(new Option("h", "prints help message"));
        options.addOption(new Option("template", true, "The template XML file to generate jobs with"));
        options.addOption(new Option("job_config", true, "The TSV file containing tab separated parameters (one job per line)"));
    }

    private  void parseCLI(String[] args) {
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("LyraPolicy", options);
            }
            template = new File(line.getOptionValue("template"));
            config = new File(line.getOptionValue("job_config"));
            SimpleDateFormat simpledatafo = new SimpleDateFormat("dd-MM-yyyy@hh-mm-ss");
            Date newDate = new Date();
            runName = simpledatafo.format(newDate);
        } catch (ParseException exp) {
            // oops, something went wrong
            LOGGER.error("Parsing failed. Reason: " + exp.getMessage());
        }
    }

    public void execute() throws Exception {
        LOGGER.info("Executing run " + runName);
        ConfigurationHandler.checkDefaultBeanDefinitions();
        XMLInterpreter.setStepLoader(ConfigurationHandler.getLoader());
        LinkedList<String> jobXMLs = XMLTemplateInterpreter.getInstance().readLocalProcessingParametersToXMLs(runName, template, config);
        //2. convert the template into jobs
        XMLJobInterpreter xmlJobInterpreter = XMLJobInterpreter.getInstance();
        for (String aString : jobXMLs) {
            ProcessingJob processingJob = xmlJobInterpreter.convertXMLtoJob(aString);
            if (processingJob.allowRun()) {
                ProcessingEngine engine = new ProcessingEngine();
                engine.runJob(processingJob);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new Launcher();
        constructOptions();
        launcher.parseCLI(args);
        launcher.execute();
    }

    public File getTemplate() {
        return template;
    }

    public void setTemplate(File template) {
        this.template = template;
    }

    public File getConfig() {
        return config;
    }

    public void setConfig(File config) {
        this.config = config;
    }

    public String getRunName() {
        return runName;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }
    
    
    
    
}
