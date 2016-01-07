/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing;

import com.compomics.pladipus.core.control.engine.callback.CallbackNotifier;
import com.compomics.pladipus.core.model.prerequisite.Prerequisite;
import com.compomics.pladipus.core.model.prerequisite.PrerequisiteParameter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Kenneth Verheggen
 */
public class ProcessingJob extends LinkedList<ProcessingStep> {

    /**
     * the required parameters for this job
     */
    private HashMap<String, String> processingParameters;
    /**
     * the unique identifier for this job
     */
    private long id;
    /**
     * a callback notifier for this process
     */
    private final CallbackNotifier notifier;
    /**
     * the user (must be in database)
     */
    private String user;
    /**
     * the name of the process
     */
    private String name;
    /**
     * the name of the run
     */
    private String run = "default";
    /**
     * The Logger Instance
     */
    private static final Logger LOGGER = Logger.getLogger(ProcessingJob.class);
    /**
     * the priority. defaults to 4 (below average)
     */
    private int priority = 4;
    /**
     * prerequisite that determines if the current machine is allowed to execute
     * this task
     */
    private final Prerequisite jobPrerequisite;

    /**
     *
     * @param processingParameters the parameters required for this job to
     * execute
     * @param user the owner of this process
     * @param id the id of this process
     * @param name the name of this process
     * @param run the parent run of this process
     */
    public ProcessingJob(HashMap<String, String> processingParameters, String user, int id, String name, String run) {
        this.user = user;
        this.id = id;
        this.name = name;
        this.run = run;
        this.processingParameters = processingParameters;
        this.jobPrerequisite = new Prerequisite();
        this.notifier = new CallbackNotifier(id);
    }

    /**
     *
     * @param processingParameters the parameters required for this job to
     * execute
     * @param user the owner of this process
     * @param id the id of this process
     * @param name the name of this process
     * @param run the parent run of this process
     * @param jobPrerequisite machine parameters that have to be respected for
     * this job
     */
    public ProcessingJob(HashMap<String, String> processingParameters, String user, int id, String name, String run, Prerequisite jobPrerequisite) {
        this.user = user;
        this.id = id;
        this.name = name;
        this.run = run;
        this.processingParameters = processingParameters;
        this.jobPrerequisite = jobPrerequisite;
        this.notifier = new CallbackNotifier(id);
    }

    /**
     *
     * @param processingParameters the parameters required for this job to
     * execute
     * @param user the owner of this process
     */
    public ProcessingJob(HashMap<String, String> processingParameters, String user) {
        this.user = user;
        this.processingParameters = processingParameters;
        this.jobPrerequisite = new Prerequisite();
        this.notifier = new CallbackNotifier();
    }

    public boolean allowRun() {
        try {
            return jobPrerequisite.checkPreRequisites();
        } catch (IOException ex) {
            LOGGER.error(ex);
            return false;
        }
    }

    public HashMap<String, String> getProcessingParameters() {
        return processingParameters;
    }

    public void setProcessingParameters(HashMap<String, String> processingParameters) {
        this.processingParameters = processingParameters;
    }

    public long getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getRun() {
        return run;
    }

    public void setProcessID(int processID) {
        this.id = processID;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return this.priority;
    }

    /**
     * This method is the inverse of convertXMLtoJob;
     *
     * @param aJob a processingjob object
     * @return the string (XML) representation of the job object
     */
    //this should be in the job object...
    public String toXML() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("job");
            rootElement.setAttribute("id", String.valueOf(getId()));
            rootElement.setAttribute("user", getUser());
            rootElement.setAttribute("run", getRun());
            doc.appendChild(rootElement);

            Element prerequisites = doc.createElement("prerequisite");
            for (PrerequisiteParameter prerequisiteParameter : jobPrerequisite.getPrerequisiteList()) {
                Element prerequisite = doc.createElement(prerequisiteParameter.toString());
                prerequisite.setAttribute("value", prerequisiteParameter.getOptionValue());
                prerequisites.appendChild(prerequisite);
            }
            rootElement.appendChild(prerequisites);

            Iterator<ProcessingStep> iterator = iterator();

            Element steps = doc.createElement("steps");
            while (iterator.hasNext()) {
                ProcessingStep aStep = iterator.next();
                //String aClass = aStep.getClass().getName();
                String aClass = aStep.getClass().getName();
                Element step = doc.createElement("step");
                step.setAttribute("class", aClass);
                steps.appendChild(step);
            }
            rootElement.appendChild(steps);

            Element parameters = doc.createElement("parameters");
            for (Map.Entry<String, String> aParameter : getProcessingParameters().entrySet()) {
                // set attributes to step element
                // System.out.println(aParameter.getKey());
                Element parameter = doc.createElement("param");
                parameter.setAttribute("name", aParameter.getKey());
                parameter.setAttribute("value", aParameter.getValue());
                parameters.appendChild(parameter);
            }
            rootElement.appendChild(parameters);

            // write the content into xml string
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String xml = writer.getBuffer().toString();
            return xml.replaceAll("\n|\r", "");
        } catch (ParserConfigurationException | TransformerException pce) {
            LOGGER.error(pce);
        }
        return "";
    }

    @Override
    public String toString() {
        StringBuilder jobAsString = new StringBuilder();
        jobAsString.append("ID\t").append(getId()).append(System.lineSeparator());
        jobAsString.append("IDENTIFIER\t").append(getName()).append(System.lineSeparator());
        int stepNr = 1;
        jobAsString.append("STEPS").append(System.lineSeparator());
        for (ProcessingStep aStep : this) {
            jobAsString.append(stepNr).append(".").append(stepNr).append("\t").append(aStep.getProcessingStepClassName()).append(System.lineSeparator());
        }
        return jobAsString.toString();
    }

    @Override
    public boolean add(ProcessingStep e) {
        e.setParameters(processingParameters);
        super.add(e);
        return true;
    }

}
