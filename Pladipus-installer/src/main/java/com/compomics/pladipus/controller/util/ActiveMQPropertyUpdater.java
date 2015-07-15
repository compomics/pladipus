/*
 * Copyright 2015 Kenneth.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.pladipus.controller.util;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Kenneth Verheggen
 */
public class ActiveMQPropertyUpdater {

    /**
     * Updates the installed activeMQ property file. NOTE activeMQ reboot is
     * required to turn this into effect
     *
     * @param activeMQXML the activeMQ configuration file
     * @param transporterHostName the name of the transporter to be updated
     * @param activeMQPort the port to be updated
     * @param jmxPort  the port to be updated for jmx
     */
    public static void updateActiveMQProperties(File activeMQXML, String transporterHostName, String activeMQPort, String jmxPort) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(activeMQXML.getAbsolutePath());

            // Get the transporter element --> carefull not to override existing user-specified settings
            Node connectors = doc.getElementsByTagName("transportConnector").item(0);
            NamedNodeMap attr = connectors.getAttributes();
            if (attr.getNamedItem("name").getTextContent().equals("openwire")) {
                //update the transporter URI
                Node nodeAttr = attr.getNamedItem("uri");
                nodeAttr.setTextContent("tcp://" + transporterHostName + ":" + activeMQPort + "?jms.prefetchPolicy.queuePrefetch=1");
            }

            //get the jmx management item
            Element jmxManagement = (Element) doc.getElementsByTagName("managementContext").item(1);
            jmxManagement.setAttribute("connectorPort", jmxPort);

// write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(activeMQXML);
            transformer.transform(source, result);

            System.out.println("Done");

        } catch (ParserConfigurationException | TransformerException | IOException | SAXException pce) {
            pce.printStackTrace();
        }
    }
}
