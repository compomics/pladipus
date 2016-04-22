/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.processing.templates;

import com.compomics.pladipus.core.model.prerequisite.Prerequisite;
import com.compomics.pladipus.core.model.prerequisite.PrerequisiteParameter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Kenneth Verheggen
 */
public class PladipusProcessingTemplate {

    /**
     * list of processing steps. This should be chronologically added to
     */
    private final LinkedList<String> processingSteps = new LinkedList<>();
    /**
     * the prerequisite object for this search. This object controls what
     * machine gets to execute this job
     */
    private Prerequisite machinePrerequisite;
    /**
     * the job specific parameters
     */
    private final TreeMap<String, ProcessingParameterTemplate> jobParameters = new TreeMap<>();
    /**
     * the run specific parameters
     */
    private final TreeMap<String, ProcessingParameterTemplate> runParameters = new TreeMap<>();
    /**
     * the user that requested this run. NOTE : SHOULD BE IN THE DATABASE !!!
     */
    private String user;
    /**
     * the name of the run
     */
    private String name;
    /**
     * the run priority. This defaults to 4 which is "below average"
     */
    private int priority = 4;
    /**
     * the id of the chain that this run will belong to
     */
    private int chainID = -1;
    /**
     * boolean indicating the order of jobs has to be respected
     */
    private boolean keepOrder;

    public PladipusProcessingTemplate(String name, String user, int priority) {
        this.name = name;
        this.user = user;
        this.priority = priority;
        this.machinePrerequisite = new Prerequisite();
    }

    /**
     *
     * @param name the name of the run
     * @param user the user submitting the run
     * @param priority the priority of the run
     * @param machinePrerequisite the prerequisite that enables a job to be run
     */
    public PladipusProcessingTemplate(String name, String user, int priority, Prerequisite machinePrerequisite) {
        this.name = name;
        this.user = user;
        this.priority = priority;
        this.machinePrerequisite = machinePrerequisite;
    }

    /**
     *
     * @return the machine prerequsites
     */
    public Prerequisite getMachinePrerequisite() {
        return machinePrerequisite;
    }

    /**
     *
     * sets the machine prerequisite
     */
    public void setMachinePrerequisite(Prerequisite machinePrerequisite) {
        this.machinePrerequisite = machinePrerequisite;
    }

    /**
     *
     * @return the chronological processingsteps
     */
    public LinkedList<String> getProcessingSteps() {
        return processingSteps;
    }

    /**
     *
     * @return Hashmap containing all processing parameters for this particular
     * template
     */
    public HashMap<String, ProcessingParameterTemplate> getAllProcessingParameters() {
        HashMap<String, ProcessingParameterTemplate> temp = new HashMap<>();
        temp.putAll(jobParameters);
        temp.putAll(runParameters);
        return temp;
    }

    /**
     *
     * @return Hashmap containing only job related processing parameters for
     * this particular template (for example the PRIDE assay to reprocess)
     */
    public TreeMap<String, ProcessingParameterTemplate> getJobParameters() {
        return jobParameters;
    }

    /**
     * s
     *
     *
     * @return Hashmap containing only run related processing parameters for
     * this particular template (for example the FASTA-file to reprocess)
     */
    public TreeMap<String, ProcessingParameterTemplate> getRunParameters() {
        return runParameters;
    }

    /**
     *
     * @return the name of the template
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     *
     * @param className the class to be executed during this to this run (adding
     * is inherently chronological)
     */
    public void addProcessingStep(String className) {
        processingSteps.add(className);
    }

    /**
     *
     * @param className the class to be executed during this to this run
     * (removal is inherently chronological)
     */
    public void removeProcessingStep(String className) {
        processingSteps.remove(className);
    }

    /**
     * Adds a parameter to a RUN, similar to a "global variable"
     *
     * @param param the parameter required to execute this step
     */
    public void addRunParameter(ProcessingParameterTemplate param) {
        if (runParameters.containsKey(param.getName())) {
            throw new IllegalArgumentException(param.getName() + " is a template parameter and cannot be updated");
        } else {
            runParameters.put(param.getName(), param);
        }
    }

    /**
     * Adds a parameter to a JOB, similar to a "local variable"
     *
     * @param param the parameter required to execute this step
     */
    public void addJobParameter(ProcessingParameterTemplate param) {
        if (runParameters.containsKey(param.getName())) {
            throw new IllegalArgumentException(param.getName() + " is a template parameter and cannot be updated");
        } else {
            jobParameters.put(param.getName(), param);
        }
    }

    /**
     * set the priority of this template
     *
     * @param priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     *
     * @return the priority
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * sets the run name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * set the run owner
     *
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        StringBuilder templateAsString = new StringBuilder("Template :" + name).append(System.lineSeparator());
        templateAsString.append("Steps :").append(System.lineSeparator());
        for (String aClassName : processingSteps) {
            templateAsString.append(aClassName).append(System.lineSeparator());
        }
        templateAsString.append("Parameters :").append(System.lineSeparator());
        templateAsString.append("Name").append("\t").append("Value").append("\t").append("Mandatory").append("\t").append("Valid").append(System.lineSeparator());
        for (ProcessingParameterTemplate aParameter : getAllProcessingParameters().values()) {
            templateAsString.append(aParameter).append(System.lineSeparator());
        }
        return templateAsString.toString();
    }

    /**
     *
     * @return the XML representation of this template. This format is used for
     * queuing
     */
    public String toXML() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("template");
            rootElement.setAttribute("user", getUser());
            rootElement.setAttribute("run", getName());
            rootElement.setAttribute("priority", String.valueOf(getPriority()));
            doc.appendChild(rootElement);

            Element prerequisites = doc.createElement("prerequisite");
            for (PrerequisiteParameter prerequisiteParameter : machinePrerequisite.getPrerequisiteList()) {
                Element prerequisite = doc.createElement(prerequisiteParameter.toString());
                prerequisite.setTextContent(prerequisiteParameter.getOptionValue());
                prerequisites.appendChild(prerequisite);
            }
            rootElement.appendChild(prerequisites);

            Element steps = doc.createElement("steps");
            for (String aStepName : getProcessingSteps()) {
                Element step = doc.createElement("step");
                step.setAttribute("class", aStepName);
                steps.appendChild(step);
            }
            rootElement.appendChild(steps);

            Element parameters = doc.createElement("parameters");
            Element globalParameters = doc.createElement("run");
            for (Map.Entry<String, ProcessingParameterTemplate> aParameter : runParameters.entrySet()) {
                // set attributes to step element
                Element parameter = doc.createElement("param");
                parameter.setAttribute("name", aParameter.getKey());
                parameter.setAttribute("value", aParameter.getValue().getValue());
                globalParameters.appendChild(parameter);
            }

            Element localParameters = doc.createElement("job");
            for (Map.Entry<String, ProcessingParameterTemplate> aParameter : jobParameters.entrySet()) {
                // set attributes to step element
                Element parameter = doc.createElement("param");
                parameter.setAttribute("name", aParameter.getKey());
                parameter.setAttribute("default", aParameter.getValue().getValue());
                parameter.setAttribute("mandatory", String.valueOf(aParameter.getValue().isMandatory()));
                localParameters.appendChild(parameter);
            }
            parameters.appendChild(globalParameters);
            parameters.appendChild(localParameters);
            rootElement.appendChild(parameters);

            // write the content into xml string
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
        return "";
    }

    /**
     * This method converts the templateXML into a jobXML. This can directly be
     * passed onto the processing engine
     *
     * @return the xml representation of this run (defaults to use process ID of
     * -1)
     */
    public String toJobXML() {
        return toJobXML(-1);
    }

    /**
     * This method converts the templateXML into a jobXML. This can directly be
     * passed onto the processing engine
     *
     * @param processID the ID for this job. This must be a unique identifier!
     * @return
     */
    public String toJobXML(int processID) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("job");
            rootElement.setAttribute("user", getUser());
            rootElement.setAttribute("run", getName());
            rootElement.setAttribute("id", String.valueOf(processID));
            rootElement.setAttribute("priority", String.valueOf(getPriority()));
            rootElement.setAttribute("chain", String.valueOf(getChainID()));
            doc.appendChild(rootElement);
            Element steps = doc.createElement("steps");

            Element prerequisites = doc.createElement("prerequisite");
            for (PrerequisiteParameter prerequisiteParameter : machinePrerequisite.getPrerequisiteList()) {
                Element prerequisite = doc.createElement(prerequisiteParameter.toString());
                prerequisite.setTextContent(prerequisiteParameter.getOptionValue());
                prerequisites.appendChild(prerequisite);
            }
            rootElement.appendChild(prerequisites);

            for (String aStepName : getProcessingSteps()) {
                Element step = doc.createElement("step");
                step.setAttribute("class", aStepName);
                steps.appendChild(step);
            }
            rootElement.appendChild(steps);
            Element parameters = doc.createElement("parameters");
            for (Map.Entry<String, ProcessingParameterTemplate> aParameter : getAllProcessingParameters().entrySet()) {
                // set attributes to step element
                Element parameter = doc.createElement("param");
                parameter.setAttribute("name", aParameter.getKey());
                parameter.setAttribute("value", aParameter.getValue().getValue());
                parameters.appendChild(parameter);
            }
            rootElement.appendChild(parameters);

            // write the content into xml string
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
        return "";
    }

    /**
     * Clears the processing step list
     */
    public void clearProcessSteps() {
        processingSteps.clear();
    }

    public void setChainID(int chainID) {
        this.chainID = chainID;
    }

    public int getChainID() {
        //check if it's in the db?
        return chainID;
    }

    public void setKeepOrder(boolean keepOrder) {
        this.keepOrder = keepOrder;
    }

    public boolean isKeepOrder() {
        return keepOrder;
    }

}
