/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.communication.interpreter.impl;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.XMLInterpreter;
import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.model.prerequisite.Prerequisite;
import com.compomics.pladipus.core.model.prerequisite.PrerequisiteParameter;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.processing.templates.ProcessingParameterTemplate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class XMLTemplateInterpreter extends XMLInterpreter {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(XMLTemplateInterpreter.class);
    /**
     * The template interpreter instance instance
     */
    private static XMLTemplateInterpreter interpreter;

    /**
     *
     * @return an instance of the XMLTempalteInterpreter
     * @throws IOException
     * @throws StepLoadingException
     */
    public static XMLTemplateInterpreter getInstance() throws IOException, StepLoadingException {
        if (interpreter == null) {
            interpreter = new XMLTemplateInterpreter();
        }
        return interpreter;
    }

    private XMLTemplateInterpreter() throws IOException, StepLoadingException {
        super();
        super.init();
    }

    /**
     *
     * @param XML is the xmlFile that will be converted to a templatejob object
     * @return the templatejob representation of the XML file
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws FileNotFoundException
     */
    public PladipusProcessingTemplate convertXMLtoTemplate(File XML) throws ParserConfigurationException, SAXException, IOException {
        StringBuilder xmlFromFile = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(XML))) {
            String line = br.readLine();
            while (line != null) {
                xmlFromFile.append(line);
                xmlFromFile.append("\n");
                line = br.readLine();
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return convertXMLtoTemplate(xmlFromFile.toString());
    }

    /**
     *
     * @param XML the string representation of a templatejob
     * @return the templatejob representation of the XML file
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public PladipusProcessingTemplate convertXMLtoTemplate(String XML) throws ParserConfigurationException, SAXException, IOException {
        PladipusProcessingTemplate template;
        Document doc = getDocumentFromXml(XML);
        String templateName = ((Element) doc.getElementsByTagName("template").item(0)).getAttribute("run");
        String userName = ((Element) doc.getElementsByTagName("template").item(0)).getAttribute("user");
        int priority = 4;
        try {
            String priorityString = ((Element) doc.getElementsByTagName("template").item(0)).getAttribute("priority");
            LOGGER.info("Priority in template : " + priorityString);
            priority = Integer.parseInt(priorityString);
        } catch (NumberFormatException e) {
            LOGGER.warn("Priority not found : default priority will be set");
        }

        Prerequisite jobPrerequisite = new Prerequisite();

        for (PrerequisiteParameter aParameter : PrerequisiteParameter.values()) {
            NodeList prerequisites = doc.getElementsByTagName(aParameter.toString());
            for (int temp = 0; temp < prerequisites.getLength(); temp++) {
                Element prerequisiteNode = (Element) prerequisites.item(temp);
                jobPrerequisite.addPrerequisite(PrerequisiteParameter.valueOf(prerequisiteNode.getNodeName().toUpperCase()), prerequisiteNode.getTextContent());
            }
        }
        //make a template job item
        template = new PladipusProcessingTemplate(templateName, userName, priority, jobPrerequisite);

        NodeList steps = doc.getElementsByTagName("step");
        for (int temp = 0; temp < steps.getLength(); temp++) {
            Node nNode = steps.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                //generate the classname
                template.addProcessingStep(eElement.getAttribute("class"));
            }
        }
        //fill the parameterlist 
        NodeList globalSection = doc.getElementsByTagName("param");
        for (int temp = 0; temp < globalSection.getLength(); temp++) {
            Node nNode = globalSection.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                //read the parameterName
                String parameterName = eElement.getAttribute("name");
                ProcessingParameterTemplate param = new ProcessingParameterTemplate(parameterName);
                boolean isRunParameter = eElement.getParentNode().getNodeName().equalsIgnoreCase("run");
                if (eElement.hasAttribute("value")) {
                    param.setValue(eElement.getAttribute("value"));
                }
                //check if the XML has a description for the parameter
                if (eElement.hasAttribute("descr")) {
                    param.setDescription(eElement.getAttribute("descr"));
                }
                if (eElement.hasAttribute("value")) {
                    param.setValue(eElement.getAttribute("value"));
                } else if (eElement.hasAttribute("default")) {
                    param.setValue(eElement.getAttribute("default"));
                }
                if (eElement.hasAttribute("mandatory") | isRunParameter) {
                    param.setMandatory();
                }
                if (isRunParameter) {
                    template.addRunParameter(param);
                } else {
                    template.addJobParameter(param);
                }
            }
        }
        return template;
    }

    /**
     * Reads the properties from a provided file and uses the provided template
     * to create job XMLs
     *
     * @param runName the runID for this run
     * @param template the provided template
     * @param templateConfig the job_config properties file
     * @return a list of JobXML
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public LinkedList<String> readLocalProcessingParametersToXMLs(String runName, PladipusProcessingTemplate template, File templateConfig) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
        LinkedList<String> Xmls = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(templateConfig))) {
            //parse the headers first to know what variables there should be and where to put them
            String[] headers = br.readLine().split("\t");
            LOGGER.debug("Checking if headers match with expected template...");
            checkCompatibility(template, headers);
            String[] parameters;
            for (String line; (line = br.readLine()) != null;) {
                // process the lines
                parameters = line.split("\t");
                for (int i = 0; i < headers.length; i++) {
                    if (!headers[i].isEmpty()) {
                        template.addJobParameter(new ProcessingParameterTemplate(headers[i], parameters[i]));
                    }
                }
                Xmls.add(template.toJobXML());
            }
        }
        return Xmls;
    }

    /**
     *
     * @param runName the runID for this run
     * @param template the provided template
     * @param templateConfig the job_config properties file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public LinkedList<HashMap<String, String>> readLocalProcessingParameters(PladipusProcessingTemplate template, File templateConfig) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
        LinkedList<HashMap<String, String>> jobParameterList = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(templateConfig))) {
            //parse the headers first to know what variables there should be and where to put them
            String[] headers = br.readLine().split("\t");
            LOGGER.debug("Checking if headers match with expected template...");
            checkCompatibility(template, headers);
            String[] parameters;
            String line;
            while ((line = br.readLine()) != null) {
                HashMap<String, String> jobParameterMap = new HashMap<>();
                // process the lines
                parameters = line.split("\t");
                for (int i = 0; i < headers.length; i++) {
                    jobParameterMap.put(headers[i], parameters[i]);
                }
                jobParameterList.add(jobParameterMap);
            }
        }
        return jobParameterList;
    }

    /**
     * Checks whether the configuration file is compatible, throws an error if it
     * doesn't match the given template
     *
     * @param template the processing template
     * @param templateConfig the configuration file for the processing template
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void checkIfConfigurationCompatible(PladipusProcessingTemplate template, File templateConfig) throws FileNotFoundException, IOException {
        //parse the headers first to know what variables there should be and where to put them
        try (BufferedReader br = new BufferedReader(new FileReader(templateConfig))) {
            //parse the headers first to know what variables there should be and where to put them
            String[] headers = br.readLine().split("\t");
            LOGGER.debug("Checking if headers match with expected template...");
            checkCompatibility(template, headers);
        }
    }

    private void checkCompatibility(PladipusProcessingTemplate template, String[] headers) throws IOException {
        List<String> headerList = Arrays.asList(headers);
        for (ProcessingParameterTemplate aParameter : template.getJobParameters().values()) {
            if (!headerList.contains(aParameter.getName())) {
                if (aParameter.isMandatory()) {
                    throw new IOException(aParameter.getName() + " is mandatory and was not found in the provided file for local parameters");
                }
            }
        }
        LOGGER.debug("Parameters are correct");
    }

    /**
     *
     * @param runName the name of the run the jobs will belong to
     * @param xMLTemplateFile the Template XML file
     * @param templateConfig the config file for local parameters (inner job
     * parameters)
     * @return a list with XMLs generated from the template and a configuration
     * file for job specific parameters
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public LinkedList<String> readLocalProcessingParametersToXMLs(String runName, File xMLTemplateFile, File templateConfig) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
        return readLocalProcessingParametersToXMLs(runName, convertXMLtoTemplate(xMLTemplateFile), templateConfig);
    }

    /**
     *
     * @param runName the name of the run the jobs will belong to
     * @param xMLTemplate the Template XML
     * @param templateConfig the config file for local parameters (inner job
     * parameters)
     * @return a list with XMLs generated from the template and a configuration
     * file for job specific parameters
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public LinkedList<String> readLocalProcessingParametersToXMLs(String runName, String xMLTemplate, File templateConfig) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
        return readLocalProcessingParametersToXMLs(runName, convertXMLtoTemplate(xMLTemplate), templateConfig);
    }

}
