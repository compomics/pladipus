/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.engine.callback;

import com.compomics.pladipus.core.model.feedback.Checkpoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
    private final ProcessBuilder processBuilder;

    /**
     *
     * @param processus the process to monitor
     */
    public ProcessingMonitor(ProcessBuilder processBuilder, CallbackNotifier notifier) {
        this.notifier = notifier;
        this.processBuilder = processBuilder;
    }

    private Exception handleError(String firstLine, BufferedReader processOutputStream) throws Exception {
        String errorLine;
        if (firstLine.toLowerCase().contains("exception:")) {
            addStackTraceElement(firstLine);
        }
        while ((errorLine = processOutputStream.readLine()) != null) {
            try {
                addStackTraceElement(errorLine);
            } catch (StringIndexOutOfBoundsException e) {
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
    public int getHook() throws IOException, InterruptedException, ExecutionException, Exception {
        //  processBuilder = processBuilder.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
        process = processBuilder.start();
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "errors");
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "process");
        errorGobbler.start();
        outputGobbler.start();
        process.waitFor();
        return process.exitValue();
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
            File logFile = new File(System.getProperty("user.home") + "/.compomics/pladipus/log/subprocess_" + type + ".log");
            if (logFile.exists()) {
                logFile.delete();
            }
            logFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(logFile, true)) {
                while ((line = br.readLine()) != null) {
                    writer.append(line).append(System.lineSeparator()).flush();
                    scanForCheckpoints(line, br);
                }
            } catch (Exception ex) {
                LOGGER.error(ex);
                ex.printStackTrace();
            }
        }

        private void writeToLog(String line, File logFile) throws IOException {

        }
    }

    private void scanForCheckpoints(String line, BufferedReader processReader) throws Exception {
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
            /*    if (!ignoreLine) {
             //scan for errors
             for (String aKeyword : errorKeyWords) {
             if (line.toUpperCase().contains(aKeyword)) {
             isAThrownError = true;
             throw (handleError(line, processReader));
             }
             }
             }*/
        } catch (NullPointerException e) {
            LOGGER.error(e);
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
