package com.compomics.pladipus.core.control.distribution.communication.interpreter;

import com.compomics.pladipus.core.control.runtime.steploader.StepLoader;
import com.compomics.pladipus.core.control.runtime.steploader.impl.SpringProcessingStepLoader;
import com.compomics.pladipus.core.model.exception.ProcessStepInitialisationException;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class XMLInterpreter {

    /**
     * The steploader to load classes that are new on the classpath
     */
    protected static StepLoader loader;

    /**
     * Overrides the step loader (for example for standalone applications). This
     * method has to be invoked before calling an instance
     *
     * @param loader the new loader
     */
    public static void setStepLoader(StepLoader loader) {
        XMLInterpreter.loader = loader;
    }

    protected void init() throws IOException, ProcessStepInitialisationException {
        if (loader == null) {
            loader = new SpringProcessingStepLoader();
        }
    }

    /**
     * Reloads the classpath
     *
     * @throws StepLoadingException
     * @throws IOException
     */
    public void refresh() throws ProcessStepInitialisationException, IOException {
        loader = new SpringProcessingStepLoader();
    }

    /**
     *
     * @param XML
     * @return the XML document object for this XML
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Document getDocumentFromXml(String XML) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new ByteArrayInputStream(XML.getBytes(Charset.forName("UTF-8"))));
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     *
     * @param loader the loader that will import the required classes
     * @param className the name of the class
     * @return
     * @throws Exception
     */
    public ProcessingStep loadProcessingStepFromClass(StepLoader loader, String className) throws ProcessStepInitialisationException {
        ProcessingStep loadProcessingStep = loader.loadProcessingStep(className);
        return loadProcessingStep;
    }
}
