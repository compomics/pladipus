package com.compomics.pladipus.core.control.engine.callback;

import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.jms.JMSException;
import org.apache.log4j.Logger;

public class ProcessingMonitor {

    /**
     * a plain LOGGER
     */
    private static Logger LOGGER = Logger.getLogger(ProcessingMonitor.class);
    /**
     * process regular inputstream = process outputstream
     */
    private InputStream ois;
    /**
     * process error-related inputstream = process outputstream
     */
    private InputStream eis;
    /**
     * Type of the detected problem
     */
    private String type;
    /**
     * The process that has been hooked by this CommandExceptionGuard
     */
    private Process process;
    /**
     * Keywords that need to be monitored
     */
    private final List<String> errorKeyWords = new ArrayList<String>();
    /**
     * Keywords that need to be monitored on success
     */
    private final List<String> keyWords = new ArrayList<String>();
    /**
     * The notifier to send back notifications
     */
    private final CallbackNotifier notifier;
    /**
     * Boolean indicating whether an error was thrown
     */
    private boolean isAThrownError;
    /**
     * A list of classes that do not need to be thrown up (DEBUGGING PURPOSE
     * ONLY !!!!)
     */
    private final String[] classesToIgnore = new String[]{"com.compomics.util.preferences.GenePreferences"};
    private final ProcessBuilder processBuilder;

    /**
     *
     * @param processBuilder
     * @param notifier
     * @param processus the process to monitor
     */
    public ProcessingMonitor(ProcessBuilder processBuilder, CallbackNotifier notifier) {
        this.notifier = notifier;
        this.processBuilder = processBuilder;
        //TODO fix this in peptideshaker, not in pladipus
        errorKeyWords.add("FATAL");
        errorKeyWords.add("PEPTDESHAKER PROCESSING CANCELED");
        errorKeyWords.add("UNABLE TO READ SPECTRUM");
        errorKeyWords.add("COMPOMICSERROR");
        errorKeyWords.add("EXCEPTION");
        errorKeyWords.add("NO MS2 SPECTRA FOUND");
    }

    private Exception handleError(String firstLine, BufferedReader processOutputStream) throws IOException {
        String errorLine;
        if (firstLine.toLowerCase().contains("exception:")) {
            System.out.println("Error encountered...");
            addStackTraceElement(firstLine);
        }
        //check the first lines for ignore classes (DEBUG ONLY)
        if ((errorLine = processOutputStream.readLine()) != null) {
            for (String ignoreClass : classesToIgnore) {
                if (errorLine.toLowerCase().contains(ignoreClass.toLowerCase())) {
                    //this class should be ignored
                    return null;
                } else {
                    addStackTraceElement(errorLine);
                }
            }
            while ((errorLine = processOutputStream.readLine()) != null) {
                try {
                    addStackTraceElement(errorLine);
                } catch (StringIndexOutOfBoundsException e) {
                }
            }
        }
        process.destroy();
        Exception reThrowable = new Exception("An error has occurred in a process !");
        reThrowable.setStackTrace(getStackTrace());
        processOutputStream.close();
        return reThrowable;
    }

    private final LinkedList<StackTraceElement> stackTraceElementList = new LinkedList<>();

    private StackTraceElement[] getStackTrace() {
        StackTraceElement[] elements = new StackTraceElement[stackTraceElementList.size()];
        stackTraceElementList.toArray(elements);
        return elements;
    }

    private void addStackTraceElement(String errorLine) throws StringIndexOutOfBoundsException {
        errorLine = errorLine.replace("at ", "");
        int endIndex = errorLine.indexOf("(");
        String declaringClass = errorLine;
        if (endIndex != -1) {
            declaringClass = errorLine.substring(0, errorLine.indexOf("("));
        }
        String method = declaringClass.substring(declaringClass.lastIndexOf(".") + 1);

        declaringClass = declaringClass.substring(0, declaringClass.lastIndexOf("."));
        String fileName = declaringClass.substring(declaringClass.lastIndexOf(".") + 1) + ".java";
        int lineNumber;
        int lineNumberIndex = errorLine.lastIndexOf(".java:") + 1;
        if (errorLine.toLowerCase().contains("native method") || lineNumberIndex == 0) {
            lineNumber = -2;
        } else {
            String lineNumberAsString = (errorLine.substring(lineNumberIndex, errorLine.lastIndexOf(")"))).replace("java:", "");
            lineNumber = Integer.parseInt(lineNumberAsString);
        }
        stackTraceElementList.add(new StackTraceElement(declaringClass, method, fileName, lineNumber));
    }

    /**
     * hooks into a process and redirects the output to the console. Also scans
     * for keywords for faulty-shutdown programs.
     *
     * @param processBuilder the processbuilder object for the process
     * @return the system.exit value
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int getHook() throws IOException, InterruptedException, ExecutionException {
        //  processBuilder = processBuilder.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
        process = processBuilder.start();
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "errors");
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "process");
        errorGobbler.start();
        outputGobbler.start();
        process.waitFor();
        return process.exitValue();
    }

    public void stopProcess() {
        process.destroyForcibly();
    }

    public void addErrorTerms(Collection<String> errorTerms) {
        this.errorKeyWords.addAll(errorTerms);
    }

    private class StreamGobbler extends Thread {

        private InputStream is;
        private final String type;

        private StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        @Override
        public void run() {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            File logFile = new File(System.getProperty("user.home") + "/pladipus/log/subprocess_" + type + ".log");
            if (logFile.exists()) {
                logFile.delete();
            }
            logFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(logFile, true)) {
                while ((line = br.readLine()) != null) {
                    if (type.equalsIgnoreCase("error")) {
                        Exception toThrow = handleError(line, br);
                        if (toThrow != null) {
                            throw toThrow;
                        }
                    } else {
                        //  System.out.println(line);
                        LOGGER.info(line);
                        writer.append(line).append(System.lineSeparator()).flush();
                        scanForCheckpoints(line);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error(ex);
                PrintStream out;
                try {
                    out = new PrintStream(logFile);
                    ex.printStackTrace(out);
                } catch (FileNotFoundException ex1) {
                    LOGGER.warn("Could not write error to file :" + ex1.getMessage());
                }
                ex.printStackTrace();
            }
        }
    }

    private void scanForCheckpoints(String line) throws PladipusProcessingException {
        boolean ignoreLine;
        ignoreLine = false;
        //print to the console
        try {
            for (Checkpoint checkpoint : notifier.getCheckpoints()) {
                if (line.toUpperCase().contains(checkpoint.getCheckpoint().toUpperCase())) {
                    notifier.onNotification(checkpoint.getFeedback(), false);
                    ignoreLine = true;
                    break;
                }
            }
            if (!ignoreLine) {
                //scan for errors
                for (String aKeyword : errorKeyWords) {
                    if (line.toUpperCase().contains(aKeyword.toUpperCase())) {
                        throw new PladipusProcessingException("An unclear error occurred in a subprocess : " + System.lineSeparator() + line);
                    }
                }
            }
        } catch (NullPointerException e) {
            LOGGER.error(e);
        } catch (SQLException | JMSException ex) {
            throw new PladipusProcessingException(ex);
        }
    }

    /**
     * hooks into a process and redirects the output to the console. Also scans
     * for keywords for faulty-shutdown programs.
     *
     * @param processBuilder the processbuilder object for the process
     * @param workingDirectory the working directory for the process (this is
     * important for certain tools that don't work with absolute paths)
     * @return the system.exit value
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int getHook(File workingDirectory) throws IOException, InterruptedException, ExecutionException, Exception {
        processBuilder.directory(workingDirectory);
        return getHook();
    }

}
