/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.playground;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.QueryOperation;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.IOException;
import java.sql.SQLException;
import javax.jms.JMSException;

/**
 *
 * @author Kenneth Verheggen
 */
public class JobPostingMockup {

    public static void main(String[] args) throws IOException, JMSException, SQLException, Exception {

        String exampleMessage = "<template run='Simple_ReSpin_2015_first_pass_crap' user='kenneth' priority='4'>"
                + " 	<steps>"
                + " 	<step class=\"com.compomics.respin.simple.processsteps.TestSyncStep\"/>"
                + "  	</steps>"
                + " 	<parameters>"
                + " 		<run>"
                + "  		</run>"
                + "   		<job>"
                + " 		</job>"
                + " 	</parameters>"
                + " </template> ";
        QueryOperation operation = new QueryOperation();

        long cumulativeQueueSize = 1;
        try {
          //  dOperation.purgeFromQueue(CompomicsQueue.UPDATE);
            cumulativeQueueSize += operation.getCumulativeQueueSize(CompomicsQueue.JOB);
        } catch (IllegalArgumentException e) {
            //then the queue still needs to be made...
        }
        PladipusProcessingTemplate convertXMLtoTemplate = XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(exampleMessage);
        String toJobXML = convertXMLtoTemplate.toJobXML((int) cumulativeQueueSize);
        try (CompomicsProducer producer = new CompomicsProducer(CompomicsQueue.JOB, toJobXML, (int) cumulativeQueueSize)) {
              Thread producerThread = new Thread(producer, "ProducerThread");
            producerThread.start();
          System.out.println(operation.getCurrentQueueSize(CompomicsQueue.JOB));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
