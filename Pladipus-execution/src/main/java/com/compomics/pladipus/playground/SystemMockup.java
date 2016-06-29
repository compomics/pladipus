/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.playground;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.IOException;
import java.sql.SQLException;
import javax.jms.JMSException;

/**
 *
 * @author Kenneth Verheggen
 */
public class SystemMockup {

    public static void main(String[] args) throws IOException, JMSException, SQLException, Exception {

        String exampleMessage = "<template run='Example' user='kenneth' priority='4'>"
                + "<steps>"
                + "<step class='com.compomics.respin.simple.processsteps.TestSyncStep'/>"
                + "</steps>"
                + "<parameters>"
                + "<run>"
                + "</run>"
                + "<job>"
                + "</job>"
                + "</parameters>"
                + "</template>";
        PladipusProcessingTemplate convertXMLtoTemplate = XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(exampleMessage);
        int processID = (int) System.currentTimeMillis();
        String toJobXML = convertXMLtoTemplate.toJobXML(processID);
        try (CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.UPDATE, processID)) {
            Thread producerThread = new Thread(producer, "ProducerThread");
            producer.addMessage(toJobXML, processID);
            producerThread.start();
            System.out.println("Posted System Job !");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
