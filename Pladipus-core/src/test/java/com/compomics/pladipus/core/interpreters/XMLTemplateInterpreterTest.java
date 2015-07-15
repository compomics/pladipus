/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.interpreters;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLTemplateInterpreter;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author Kenneth Verheggen
 */
public class XMLTemplateInterpreterTest extends TestCase {

    public XMLTemplateInterpreterTest(String testName) {
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
     * Test of convertXMLtoTemplateJob method, of class
     * XMLTemplateJobInterpreter.
     */
    public void testConvertXMLtoTemplateJob() throws Exception {
        System.out.println("convertXMLtoTemplateJob");
        System.out.println("Loading template XML");
        File templateXML = new File(XMLTemplateInterpreterTest.class.getResource("/PLADIPUS_TEST_RUN.xml").getFile());
        PladipusProcessingTemplate result = XMLTemplateInterpreter.getInstance().convertXMLtoTemplate(templateXML);
        
        System.out.println(result.toJobXML(299));
        //TEST RUN HEADER PARSING
        assertEquals("PLADIPUS_TEST_RUN", result.getName());
        assertEquals("PLADIPUS_TESTER", result.getUser());
        //TEST RUN STEP CONTENT SIZE
        assertEquals(4, result.getProcessingSteps().size());

        //TEST RUN PARAMETERS
        assertTrue(result.getAllProcessingParameters().get("fastafile").isMandatory());
        System.out.println(result.getAllProcessingParameters().get("fastafile"));
        assertEquals("/mnt/pladipus/Pladipus/data/Fasta/Uniprot/UP000005640_9606_concatenated_target_decoy.fasta", result.getAllProcessingParameters().get("fastafile").getValue());
        assertTrue(result.getAllProcessingParameters().get("outputFolder").isMandatory());
        assertEquals("/mnt/pladipus/Pladipus/data/ReSpin/UniProt_Human_First_Pass_cRAP/", result.getAllProcessingParameters().get("outputFolder").getValue());
        assertTrue(result.getAllProcessingParameters().get("SearchGUI").isMandatory());
        assertEquals("/mnt/pladipus/Pladipus/external/SearchGUI/", result.getAllProcessingParameters().get("SearchGUI").getValue());
        assertTrue(result.getAllProcessingParameters().get("PeptideShaker").isMandatory());
        assertEquals("/mnt/pladipus/Pladipus/external/PeptideShaker/", result.getAllProcessingParameters().get("PeptideShaker").getValue());

        //TEST JOB PARAMETERS
        assertTrue(result.getAllProcessingParameters().get("peakfile").getValue().equalsIgnoreCase("default"));
        assertTrue(result.getAllProcessingParameters().get("parameterfile").getValue().equalsIgnoreCase("default"));
        assertTrue(result.getAllProcessingParameters().get("assay").getValue().equalsIgnoreCase("default"));
        
        //CHECK RUN PREREQUISITE
        assertFalse(result.getMachinePrerequisite().checkPreRequisites());

    }

}
