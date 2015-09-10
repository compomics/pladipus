/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.engine.callback;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen NOTE this class may be obsolete in the future. This
 * was created to circumvent SearchGUI / PeptideShaker to hang, causing the
 * entire system to block
 */
public class ProcessingMonitor implements Callable<Integer> {

    /**
     * a plain LOGGER
     */
    Logger LOGGER = Logger.getLogger(ProcessingMonitor.class);
    /**
     * process regular inputstream = process outputstream
     */
    private static InputStream ois;
    /**
     * process error-related inputstream = process outputstream
     */
    private static InputStream eis;
    /**
     * Type of the detected problem
     */
    private String type;
    /**
     * The process that has been hooked by this CommandExceptionGuard
     */
    private static Process process;
    /**
     * Keywords that need to be monitored
     */
    private static final List<String> errorKeyWords = new ArrayList<String>();
    /**
     * Keywords that need to be monitored on success
     */
    private static final List<String> keyWords = new ArrayList<String>();
    /**
     * Keywords that need no monitoring (dirty peptideshaker solution...)
     */
    private static final List<String> ignoreKeyWords = new ArrayList<String>();
    /**
     * Flag that marks if an error has been thrown
     */
    private boolean isAThrownError = false;

    /**
     *
     * @param processus the process to monitor
     */
    public ProcessingMonitor(Process processus) {
        ProcessingMonitor.process = processus;
        ProcessingMonitor.ois = processus.getInputStream();
        ProcessingMonitor.eis = processus.getErrorStream();
        type = "ERROR";

        //TODO FIGURE OUT A CLEANER WAY TO DO THIS?...
        errorKeyWords.add("ERROR");
        errorKeyWords.add("FATAL");
        errorKeyWords.add("PLEASE CONTACT");
        errorKeyWords.add("EXCEPTION");
        errorKeyWords.add("PEPTDESHAKER PROCESSING CANCELED");
        errorKeyWords.add("UNABLE TO READ SPECTRUM");
        errorKeyWords.add("COMPOMICSERROR");

        keyWords.add("SEARCH COMPLETED");
        keyWords.add("NO IDENTIFICATIONS RETAINED");
        keyWords.add("END OF PEPTIDESHAKER");

    }

    @Override
    public Integer call() throws Exception {
        InputStream mergedInputStream = new SequenceInputStream(ois, eis);
        try {
            BufferedReader processOutputStream = new BufferedReader(new InputStreamReader(mergedInputStream));
            String line;
            LOGGER.debug("An errorguard was hooked to the process.");
            boolean ignoreLine;
            while ((line = processOutputStream.readLine()) != null) {
                ignoreLine = false;
                LOGGER.info(line);
                try {
                    for (String aKeyword : ignoreKeyWords) {
                        if (line.toUpperCase().contains(aKeyword)) {
                            ignoreLine = true;
                            break;
                        }
                    }
                    if (!ignoreLine) {
                        for (String aKeyword : keyWords) {
                            if (line.toUpperCase().contains(aKeyword)) {
                                process.destroy();
                                mergedInputStream.close();
                                return 0;
                            }
                        }
                        for (String aKeyword : errorKeyWords) {
                            if (line.toUpperCase().contains(aKeyword)) {
                                isAThrownError = true;
                                throw (handleError(line, processOutputStream));
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    LOGGER.error(e);
                }
            }
        } catch (NullPointerException | IOException ex) {
            LOGGER.error(ex);
        }
        return 0;
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
    public static int getHook(ProcessBuilder processBuilder) throws IOException, InterruptedException, ExecutionException {
        processBuilder.redirectOutput(Redirect.INHERIT);
        processBuilder.redirectError(Redirect.INHERIT);
        process = processBuilder.start();

        /*     ExecutorService pool = Executors.newFixedThreadPool(1);
        Callable<Integer> callable = new ProcessingMonitor(process);
        Future<Integer> future = pool.submit(callable);
        //wait for the process, the hook will kill it if needed...
        pool.shutdown();
        process.waitFor();
        int systemExitValue = 0;
        try {
        systemExitValue = future.get();
        } catch (Throwable e) {
        e.printStackTrace();
        }
        // pool.shutdownNow();*/
        InputStream processOutput = process.getInputStream();
        while(processOutput.read()>=0){
            //wait?
        }
        process.waitFor();
        return process.exitValue();
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
    public static int getHook(ProcessBuilder processBuilder, File workingDirectory) throws IOException, InterruptedException, ExecutionException {
        processBuilder.directory(workingDirectory);
        return getHook(processBuilder);
    }

}
