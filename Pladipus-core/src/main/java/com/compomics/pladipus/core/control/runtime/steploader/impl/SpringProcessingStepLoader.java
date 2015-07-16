/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.runtime.steploader.impl;

import com.compomics.pladipus.core.control.runtime.steploader.StepLoader;
import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.control.updates.ProcessingBeanUpdater;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author Kenneth Verheggen
 */
public class SpringProcessingStepLoader implements StepLoader {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(SpringProcessingStepLoader.class);
    /**
     * The folder that has to be loaded into the classpath
     */

    private File repositoryFolder;
    /**
     * The networkproperties
     */

    private final NetworkProperties props;
    /**
     * The applicationcontext for the loaded classes
     */
    private final FileSystemXmlApplicationContext appContext;

    /**
     * 
     * @throws IOException
     * @throws StepLoadingException
     */
    public SpringProcessingStepLoader() throws IOException, StepLoadingException {
        LOGGER.debug("Refreshing spring context");
        //Logger.getRootLogger().setLevel(Level.OFF);
        props = NetworkProperties.getInstance();
        //update classpath in case new jars were added
        reloadRepositoryPath();
        //reload appContext
        //change this to the "outside" property file?
        try {
            File processingBeanFile = ProcessingBeanUpdater.getInstance().getProcessingBeanConfigFile();
            appContext = new FileSystemXmlApplicationContext(processingBeanFile.getAbsolutePath());
        } catch (BeansException e) {
            Logger.getRootLogger().setLevel(Level.toLevel(NetworkProperties.getInstance().getLoggingLevel()));
            throw new StepLoadingException("Bean not found : " + e);
        }
    }

    private void reloadRepositoryPath() throws StepLoadingException {
        repositoryFolder = new File(props.getAdditionalClasspath());
        /*    try {
         FolderSynchronizer folderSynchronizer = FolderSynchronizerFactory.getFolderSynchronizer(URI.create(props.getRemoteLibraryRepository()));
         folderSynchronizer.synchronize(repositoryFolder);
         } catch (java.net.ConnectException e) {
         LOGGER.error("Could not sync with the online repository");
         }*/
        addAllToClassPath(repositoryFolder);
    }

    private void addAllToClassPath(File dir) throws StepLoadingException {
        List<String> classPathFiles = Arrays.asList(System.getProperty("java.class.path").split(";"));
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (!classPathFiles.contains(file.getAbsolutePath())) {
                    try {
                        addToClassPath(file.getAbsolutePath());
                    } catch (MalformedURLException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new StepLoadingException(e);
                    }
                    if (file.isDirectory()) {
                        addAllToClassPath(file);
                    }
                }
            }
        }
    }

    private void addToClassPath(String s) throws MalformedURLException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        File f = new File(s);
        URL u = f.toURI().toURL();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{u});
    }

    @Override
    public ProcessingStep loadProcessingStep(String className) throws Exception {
        LOGGER.info("Loading " + className);
        //load the bean
        // Logger.getRootLogger().setLevel(Level.OFF);
        ProcessingStep assummedProcessingStep = null;
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        try {
            //check if it's already on the classpath?
            LOGGER.debug("Loading from classpath resource");
            assummedProcessingStep = (ProcessingStep) systemClassLoader.loadClass(className).newInstance();
        } catch (InstantiationException | ClassNotFoundException ex) {
            LOGGER.debug("Importing via spring...");
            String myClass = className.substring(className.lastIndexOf(".") + 1);
            try {
                assummedProcessingStep = (ProcessingStep) appContext.getBean(myClass);
                if (assummedProcessingStep == null) {
                    throw new Exception(className + " is not on the classpath, nor found in the external library folder");
                }
                assummedProcessingStep.setProcessingStepClassName(className);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e);
            }
        }
        //  Logger.getRootLogger().setLevel(Level.toLevel(NetworkProperties.getInstance().getLoggingLevel()));
        return assummedProcessingStep;
    }

}
