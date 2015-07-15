/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.updates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessingBeanUpdater {

    /**
     * The File that contains the bean definitions that can be loaded into the
     * classpath
     */

    private static File beanXMLDefinitionFile;
    /**
     * The ProcessingBeanUpdater instance
     */

    private static ProcessingBeanUpdater instance;
    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(ProcessingBeanUpdater.class);

    private ProcessingBeanUpdater() {
    }

    /**
     *
     * @return the ProcessingBeanUpdater
     */
    public static ProcessingBeanUpdater getInstance() {
        if (instance == null) {
            instance = new ProcessingBeanUpdater();
            beanXMLDefinitionFile = new File(System.getProperty("user.home") + "/.compomics/pladipus/config/processing-beans.xml");
            if (!beanXMLDefinitionFile.exists()) {
                copyFromResources();
            }
        }
        return instance;
    }

    /**
     *
     * @return the bean definition file
     */
    public File getProcessingBeanConfigFile() {
        return beanXMLDefinitionFile;
    }

    private static void copyFromResources() {
        //loads the codontable from within the jar...
        if (beanXMLDefinitionFile != null && !beanXMLDefinitionFile.exists()) {
            try {
                beanXMLDefinitionFile.getParentFile().mkdirs();
                beanXMLDefinitionFile.createNewFile();
                InputStream inputStream = new ClassPathResource("processing-beans.xml").getInputStream();
                OutputStream outputStream = new FileOutputStream(beanXMLDefinitionFile);
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }
    }

    /**
     * Adds a new class to the bean definition
     * @param fullyDefinedClassName the fully defined class name
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws JDOMException
     */
    public void addNewProcessingStep(String fullyDefinedClassName) throws IllegalArgumentException, IOException, JDOMException {
        String className = fullyDefinedClassName.substring(fullyDefinedClassName.lastIndexOf(".") + 1);

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(beanXMLDefinitionFile);

        //check if the class is not already in there
        for (Element aBean : document.getRootElement().getChildren()) {
            if (aBean.getAttribute("class").getValue().equals(fullyDefinedClassName)) {
                throw new IllegalArgumentException("Class is already defined in the bean configuration for " + aBean.getAttributeValue("id"));
            } else if (aBean.getAttribute("id").getValue().equals(className)) {
                throw new IllegalArgumentException("Classname is already in use");
            }
        }

        Element newClassElement = new Element("bean")
                .setAttribute("id", className)
                .setAttribute("class", fullyDefinedClassName)
                .setAttribute("lazy-init", "true");
        document.getRootElement().addContent(newClassElement);
        XMLOutputter outputter = new XMLOutputter();
        try (StringWriter stringWriter = new StringWriter(); FileWriter writer = new FileWriter(beanXMLDefinitionFile);) {
            outputter.output(document, stringWriter);
            String output = stringWriter.getBuffer().toString();
            //remove empty namespaces
            output = output.replace(" xmlns=\"\"", "");
            writer.append(output);
        }
    }

    /**
     *
     * @return a map of the installed process step names with the fully defined classnames 
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public TreeMap<String, String> getInstalledProcessStepClasses() throws ParserConfigurationException, IOException, SAXException {
        TreeMap<String, String> installedProcesses = new TreeMap<>();
        File propertiesFile = new File(System.getProperty("user.home") + "/.compomics/pladipus/config/processing-beans.xml");

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = docBuilder.parse(propertiesFile.getAbsolutePath());

        // Get the bean element --> carefull not to override existing user-specified settings
        NodeList connectors = doc.getElementsByTagName("bean");
        for (int i = 0; i <= connectors.getLength(); i++) {
            Node item = connectors.item(i);
            if (item != null) {
                Node idItem = item.getAttributes().getNamedItem("id");
                Node classItem = item.getAttributes().getNamedItem("class");
                installedProcesses.put(idItem.getNodeValue(), classItem.getNodeValue());
            }
        }
        return installedProcesses;
    }

}
