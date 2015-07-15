/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.interpreters;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author Kenneth Verheggen
 */
public class XMLJobInterpreterTest extends TestCase {

    public XMLJobInterpreterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of convertXMLtoJob method, of class XMLJobInterpreter.
     */
    public void testConvertXMLtoJob() throws Exception {
        System.out.println("convertXMLtoJob");
        File templateXML = new File(XMLTemplateInterpreterTest.class.getResource("/PLADIPUS_TEST_JOB.xml").getFile());
        ProcessingJob instance = XMLJobInterpreter.getInstance().convertXMLtoJob(templateXML);

        //TEST IF JOB CAN BE USED
        assertTrue(instance.allowRun());
        //TEST JOB HEADERS
        assertEquals("PLADIPUS_TESTER_299", instance.getName());
        assertEquals("PLADIPUS_TESTER",instance.getUser());
        assertEquals(299,instance.getId());
        //TEST PROCESSING STEPS
        assertEquals(4, instance.size());
        //TEST RUN PARAMETERS
        assertEquals("/mnt/pladipus/Pladipus/data/Fasta/Uniprot/UP000005640_9606_concatenated_target_decoy.fasta", instance.getProcessingParameters().get("fastafile"));
        assertEquals("/mnt/pladipus/Pladipus/data/ReSpin/UniProt_Human_First_Pass_cRAP/", instance.getProcessingParameters().get("outputFolder"));
        assertEquals("/mnt/pladipus/Pladipus/external/SearchGUI/", instance.getProcessingParameters().get("SearchGUI"));
        assertEquals("/mnt/pladipus/Pladipus/external/PeptideShaker/", instance.getProcessingParameters().get("PeptideShaker"));

        //TEST JOB PARAMETERS
        assertTrue(instance.getProcessingParameters().get("peakfile").equalsIgnoreCase("default"));
        assertTrue(instance.getProcessingParameters().get("parameterfile").equalsIgnoreCase("default"));
        assertTrue(instance.getProcessingParameters().get("assay").equalsIgnoreCase("default"));

    }

}
