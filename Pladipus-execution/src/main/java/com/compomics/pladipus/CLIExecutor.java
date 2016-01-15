package com.compomics.pladipus;

import com.compomics.pladipus.core.control.distribution.PladipusTrafficManager;
import com.compomics.pladipus.core.control.distribution.service.RunService;
import com.compomics.pladipus.core.control.distribution.service.UserService;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsQueueConnectionFactory;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.util.ProcessAction;
import com.compomics.pladipus.util.RunAction;
import com.compomics.pladipus.view.MainGUI;
import java.io.Console;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.sql.SQLException;
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
 * @author Kenneth Verheggen
 */
public class CLIExecutor {

    /**
     * the supplied options for the command
     */
    private static Options options;
    /**
     * boolean to indicate a pull or push operation (push = true)
     */
    private static boolean push;
    /**
     * the supplied template file
     */
    private static File templateFile;
    /**
     * the supplied configuration file
     */
    private static File jobConfigurationFile;
    /**
     * the logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CLIExecutor.class);
    /**
     * boolean indicating if all jobs should be pushed from the database
     */
    private static boolean pushFromLocal = false;
    /**
     * the current user
     */
    private static String user;
    /**
     * the password for the current user
     */
    private static String password;
    /**
     * boolean indicating if the worker can pull tasks indefinitely
     */
    private static boolean auto_pull = false;
    /**
     * boolean indicating if a run can be automatically started after pushing
     */
    private static boolean auto_start = false;
    /**
     * the default run priority
     */
    private static int priority = 4;
    /**
     * the PladipusTrafficManager instance
     */
    private static final PladipusTrafficManager trafficManager = PladipusTrafficManager.getInstance();

    public static void main(String[] args) throws Exception {
        //if the settings folder has been made before...
        if (args.length == 0) {
            MainGUI.main(args);
        } else {
            while (true) {
                //check if this is the firt time pladipus is run...
                File firstRunFile = new File(System.getProperty("user.home") + "/pladipus/config");
                if (!firstRunFile.exists()) {
                    NetworkProperties.getInstance();
                    System.out.println("Hello! It seems this is the first time you are running pladipus." + System.lineSeparator()
                            + "Some settings are required for the software to become operational" + System.lineSeparator()
                            + "You can find these in the following folder : " + System.lineSeparator()
                            + firstRunFile.getAbsolutePath() + System.lineSeparator()
                            + "Hope to see you soon !");
                    //wait to make sure the user read it
                    Console c = System.console();
                    if (c != null) {
                        c.format("\nPress ENTER to exit.\n");
                        c.readLine();
                        System.exit(0);
                    }
                } else if (trafficManager.isSystemOnline()) {
                    constructOptions();
                    parseCLI(args);
                    if (push && templateFile != null) {
                        int runID = trafficManager.pushToPladipus(templateFile, jobConfigurationFile);
                        if (auto_start) {
                            RunAction.startRuns(runID);
                            auto_start = false;
                        }
                    } else {
                        System.out.println("Hello, pladipus will now start pulling jobs and updates from the controller.");
                        System.out.println("Thank you for participating in the research!");
                        while (true) {
                            try {
                                trafficManager.pullFromPladipus();
                                if (!auto_pull) {
                                    break;
                                }
                            } catch (UnknownHostException e) {
                                LOGGER.error(e);
                                try {
                                    Thread.sleep(10000);
                                    CompomicsQueueConnectionFactory.reset();
                                } catch (InterruptedException e2) {

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean login() {
        boolean accept = false;
        UserService uService = UserService.getInstance();
        try {
            if (!uService.userExists(user)) {
                throw new SecurityException(user + " was not found !");
            } else if (uService.verifyUser(user, password)) {
                accept = true;
            } else {
                throw new SecurityException(user + " is not a authorized to push jobs !");
            }
        } catch (SQLException | UnsupportedEncodingException ex) {
            LOGGER.error(ex);
        }
        return accept;
    }

    private static boolean runExists(int run_id) {
        boolean exists = false;
        RunService rService = RunService.getInstance();
        try {
            exists = rService.runExists(run_id);
            if (!exists) {
                LOGGER.error(run_id + " does not exist!");
            }
        } catch (SQLException ex) {
            LOGGER.error(ex);
        }
        return exists;
    }

    private static void parseCLI(String[] args) {
        LOGGER.debug("Parsing arguments...");
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("LyraPolicy", options);
            }
            //verify the user
            if (line.hasOption("u")) {
                user = line.getOptionValue("u");
            } else {
                throw new SecurityException("User is not provided");
            }
            System.out.println("user is " + user);
//verify the user
            if (line.hasOption("p")) {
                password = line.getOptionValue("p");
            } else {
                LOGGER.warn("No password was provided...");
                password = "";
            }

            if (login()) {
                //check for pushing options
                if (line.hasOption("push")) {
                    push = true;
                    if (line.hasOption("l")) {
                        pushFromLocal = true;
                    } else if (line.hasOption("template")) {
                        templateFile = new File(line.getOptionValue("template"));
                    } else if (!pushFromLocal) {
                        throw new ParseException("The template should be declared !");
                    }
                    if (line.hasOption("job_config")) {
                        jobConfigurationFile = new File(line.getOptionValue("job_config"));
                    } else if (!pushFromLocal) {
                        throw new ParseException("The job configuration should be declared !");
                    }
                    if (line.hasOption("auto_start")) {
                        auto_start = true;
                    }
                } else if (line.hasOption("pull")) {
                    push = false;
                } else if (line.hasOption("auto_pull")) {
                    push = false;
                    auto_pull = true;
                } else if (line.hasOption("start_process")) {
                    push = false;
                    auto_pull = false;
                    //get the target run
                    int[] targetValues = validateTargetIds(line, "start_process");
                    ProcessAction.startProcesses(targetValues);
                } else if (line.hasOption("stop_process")) {
                    push = false;
                    auto_pull = false;
                    //get the target run
                    int[] targetValues = validateTargetIds(line, "stop_process");
                    ProcessAction.stopProcess(targetValues);
                } else if (line.hasOption("start_run")) {
                    push = false;
                    auto_pull = false;
                    //get the target run
                    int[] targetValues = validateTargetIds(line, "start_run");
                    RunAction.startRuns(targetValues);
                } else if (line.hasOption("stop_run")) {
                    push = false;
                    auto_pull = false;
                    //get the target run
                    int[] targetValues = validateTargetIds(line, "stop_run");
                    RunAction.stopRuns(targetValues);
                } else {
                    throw new ParseException("Please specify the action :  start_process,stop_process,start_run,stop_run,push, pull or auto_pull");
                }

            }
        } catch (ParseException exp) {
            // oops, something went wrong
            LOGGER.error("Parsing failed. Reason: " + exp.getMessage());
        }
    }

    private static int[] validateTargetIds(CommandLine line, String commandLineOption) {
        String[] targetIDs = line.getOptionValue(commandLineOption).split(",");
        boolean proceed = false;
        int[] targetIdValues = new int[targetIDs.length];
        for (int i = 0; i <= targetIDs.length; i++) {
            try {
                targetIdValues[i] = Integer.parseInt(targetIDs[i]);
                proceed = runExists(targetIdValues[i]);
            } catch (NumberFormatException e) {
                LOGGER.error(e);
                proceed = false;
                break;
            }
        }
        if (!proceed) {
            throw new IllegalArgumentException("Invalid values submitted by user");
        }
        return targetIdValues;
    }

    private static void constructOptions() {
        //options
        options = new Options();
        options.addOption(new Option("help", "prints help message"));
        options.addOption(new Option("u", true, "pladipus user"));
        options.addOption(new Option("p", true, "pladipus password"));
        //pushing options
        options.addOption(new Option("template", true, "The template XML file to generate jobs with"));
        options.addOption(new Option("job_config", true, "The TSV file containing tab separated parameters (one job per line)"));
        options.addOption(new Option("push", "Push jobs to Pladipus"));
        options.addOption(new Option("auto_start", "Starts a run automatically after pushing it"));
        //pulling options
        options.addOption(new Option("pull", "Pull a job from Pladipus"));
        options.addOption(new Option("auto_pull", "Pull jobs from Pladipus automatically when available"));
        //starting/stopping options
        options.addOption(new Option("start_process", true, "Start a (or multiple) stored process(es) on Pladipus"));
        options.addOption(new Option("stop_process", true, "Stop a (or multiple) running process(es) on Pladipus"));
        options.addOption(new Option("start_run", true, "Start a (or multiple) stored run(s) on Pladipus"));
        options.addOption(new Option("stop_run", true, "Stop a (or multiple) running run(s) on Pladipus"));
    }

}
