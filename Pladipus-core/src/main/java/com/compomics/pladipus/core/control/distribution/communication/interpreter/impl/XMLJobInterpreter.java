package com.compomics.pladipus.core.control.distribution.communication.interpreter.impl;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.XMLInterpreter;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.exception.XMLInterpreterException;
import com.compomics.pladipus.core.model.prerequisite.Prerequisite;
import com.compomics.pladipus.core.model.prerequisite.PrerequisiteParameter;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
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
public class XMLJobInterpreter extends XMLInterpreter {

    /**
     * The logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(XMLJobInterpreter.class);
    /**
     * The Default job interpreter instance
     */
    private static XMLJobInterpreter interpreter;

    /**
     *
     * @return an instance of a XMLJobInterpreter
     * @throws XMLInterpreterException
     * @throws java.io.IOException
     */
    public static XMLJobInterpreter getInstance() throws XMLInterpreterException, IOException, ProcessStepInitialisationException {
        if (interpreter == null) {
            interpreter = new XMLJobInterpreter();
        }
        return interpreter;
    }

    private XMLJobInterpreter() throws XMLInterpreterException, IOException, ProcessStepInitialisationException {
        super();
        super.init();
    }

    /**
     *
     * @param XML is the xmlFile that will be converted to a processjob object
     * @return the processingjob representation of the XML file
     * @throws FileNotFoundException
     */
    public ProcessingJob convertXMLtoJob(File XML) throws XMLInterpreterException, SAXException, IOException, Exception {
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
        return convertXMLtoJob(xmlFromFile.toString());
    }

    /**
     *
     * @param XML the string representation of a processingjob
     * @return the processingjob representation of the XML file
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    public ProcessingJob convertXMLtoJob(String XML) throws ParserConfigurationException, SAXException, IOException, ProcessStepInitialisationException {
        LOGGER.info("Converting instructions to java code...");
        Document doc = getDocumentFromXml(XML);
        //option   Document doc = getDocumentFromXml(XML);al, but recommended
        LOGGER.debug("Extracting message parameters...");
        String user = ((Element) doc.getElementsByTagName("job").item(0)).getAttribute("user");

        String id = ((Element) doc.getElementsByTagName("job").item(0)).getAttribute("id");

        String run = ((Element) doc.getElementsByTagName("job").item(0)).getAttribute("run");

        String chain = ((Element) doc.getElementsByTagName("job").item(0)).getAttribute("chain");

        LOGGER.debug(run + "(" + id + ") was requested by " + user);

        HashMap<String, String> parameterMap = new HashMap<>();
        Prerequisite jobPrerequisite = new Prerequisite();

        for (PrerequisiteParameter aParameter : PrerequisiteParameter.values()) {
            NodeList prerequisites = doc.getElementsByTagName(aParameter.toString());
            for (int temp = 0; temp < prerequisites.getLength(); temp++) {
                Element prerequisiteNode = (Element) prerequisites.item(temp);
                jobPrerequisite.addPrerequisite(PrerequisiteParameter.valueOf(prerequisiteNode.getNodeName().toUpperCase()), prerequisiteNode.getTextContent());
            }
        }

        NodeList parameters = doc.getElementsByTagName("param");
        for (int temp = 0; temp < parameters.getLength(); temp++) {
            Element parameterNode = (Element) parameters.item(temp);
            parameterMap.put(parameterNode.getAttribute("name"), parameterNode.getAttribute("value"));
        }
        //add the run information (for example output purposes)
        parameterMap.put("run", run);
        parameterMap.put("user", user);
        parameterMap.put("processID", id);
        ProcessingJob job = new ProcessingJob(parameterMap, user, Integer.parseInt(id), user + "_" + id, run, jobPrerequisite);
        //add the chain to which this job belongs
        int chainid = -1;
        if (chain != null && !chain.isEmpty()) {
            chainid = Integer.parseInt(chain);
        }
        job.setIdChain(chainid);

        //add the standard initialisingStep
        //download required
        if (parameterMap.containsKey("required")) {
            LOGGER.debug("Some additional files will be downloaded to run this job...");
            downloadRequiredProjects(parameterMap.get("required"));
        }
        NodeList steps = doc.getElementsByTagName("step");
        for (int temp = 0; temp < steps.getLength(); temp++) {
            Node nNode = steps.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element stepNode = (Element) steps.item(temp);
                String className = stepNode.getAttribute("class");
                LOGGER.debug("Loading " + className);
                ProcessingStep requestedStep = loadProcessingStepFromClass(loader, className);
                requestedStep.setProcessingID(Integer.parseInt(id));
                job.add(requestedStep);
            }
        }
        return job;
    }

    private void downloadRequiredProjects(String remoteProjectFolder) throws IOException, ProcessStepInitialisationException {
        //this can be a list of multiple later?...
        //TODO replace this by URI's
        if (remoteProjectFolder != null && !remoteProjectFolder.isEmpty()) {
            LOGGER.info("Downloading " + remoteProjectFolder);
            File remoteProjectFiles = new File(remoteProjectFolder);
            if (remoteProjectFiles.exists()) {
                NetworkProperties props = NetworkProperties.getInstance();
                File localLibraryRepository = new File(props.getAdditionalClasspath());
                File localProjectFiles = new File(localLibraryRepository, remoteProjectFiles.getName());
                if (!localProjectFiles.exists()) {
                    FileUtils.deleteDirectory(localProjectFiles);
                    localProjectFiles.mkdirs();
                    FileUtils.copyDirectory(remoteProjectFiles, localLibraryRepository);
                }
                refresh();
            } else {
                throw new IOException("Remote location could not be contacted");
            }
        }
    }

}
