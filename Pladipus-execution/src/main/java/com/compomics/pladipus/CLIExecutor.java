/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus;

import com.compomics.pladipus.core.control.distribution.PladipusTrafficManager;
import com.compomics.pladipus.core.control.distribution.service.UserService;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import com.compomics.pladipus.view.MainGUI;
import com.sun.mail.iap.ConnectionException;
import java.io.Console;
import java.io.EOFException;
import java.io.File;
import java.io.UnsupportedEncodingException;
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
    private static boolean auto = false;
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
                try {
                    //check if this is the firt time pladipus is run...
                    File firstRunFile = new File(System.getProperty("user.home") + "/.compomics/pladipus/config");
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
                            trafficManager.pushToPladipus(templateFile, jobConfigurationFile);
                        } else {
                            trafficManager.pullFromPladipus();
                            //continue doing this if it's auto-pulling mode
                            while (auto) {
                                trafficManager.pullFromPladipus();
                            }
                        }
                        LOGGER.info("Done");
                        System.exit(0);
                    }
                } catch (ConnectionException | EOFException e) {
                    LOGGER.error("Could not connect to server...");
                    Thread.sleep(5000);
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
            System.out.println("user is "+user);
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
                } else if (line.hasOption("pull")) {
                    push = false;
                } else if (line.hasOption("auto_pull")) {
                    push = false;
                    auto = true;
                } else {
                    throw new ParseException("Please specify the action :  push, pull or auto_pull");
                }

            }
        } catch (ParseException exp) {
            // oops, something went wrong
            LOGGER.error("Parsing failed. Reason: " + exp.getMessage());
        }
    }

    private static void constructOptions() {
        //options
        options = new Options();
        options.addOption(new Option("help", "prints help message"));
        options.addOption(new Option("u",true, "pladipus user"));
        options.addOption(new Option("p",true, "pladipus password"));
        //pushing options
        options.addOption(new Option("template", true, "The template XML file to generate jobs with"));
        options.addOption(new Option("job_config", true, "The TSV file containing tab separated parameters (one job per line)"));
        options.addOption(new Option("push", "Push jobs to Pladipus"));
        //pulling options
        options.addOption(new Option("pull", "Pull a job from Pladipus"));
        options.addOption(new Option("auto_pull", "Pull jobs from Pladipus automatically when available"));
    }

}
