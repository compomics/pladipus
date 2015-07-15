/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.database.dao.impl;

import com.compomics.pladipus.core.control.distribution.communication.interpreter.impl.XMLJobInterpreter;
import com.compomics.pladipus.core.database.MockDatabaseTest;
import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.templates.PladipusProcessingTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Kenneth Verheggen
 */
public class DatabaseIntegrationTest extends MockDatabaseTest {

    public DatabaseIntegrationTest(String testName) {
        super(testName);
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private final RunDAO rInstance = RunDAO.getInstance();
    private final ProcessDAO pInstance = ProcessDAO.getInstance();

    /**
     * Test of createRun method, of class RunDAO.
     */
    @Test
    public void testCreateRun() throws Exception {
        String runName = "Pladipus_Test_Run_2";
        String user = "pladmin";
 
        PladipusProcessingTemplate template = new PladipusProcessingTemplate(runName,user, 4);
        template.addProcessingStep("com.compomics.respin.simple.processsteps.TestingStep");
        System.out.print("Creating Run...");
        int result = rInstance.createRun(template);
        System.out.print("~ Done");

        System.out.println("Checking Run Size..");
        assertEquals(1, rInstance.getRuns("pladmin").size());
        System.out.print("~ Done");

        System.out.println("Checking Run Contents...");
        Integer runID = rInstance.getRunID(runName, user);
        assertTrue(runID == result);
        assertEquals(runName, rInstance.getRunTitle(runID));
        assertEquals("pladipus@ugent.be", rInstance.getOwnerContact(1));
        assertEquals(user, rInstance.getRunOwner(runID));
        assertEquals(1, rInstance.getRuns(user).size());
        System.out.print("~ Done");

        System.out.println("Checking Inclusion of Process..");
        List<HashMap<String, String>> processParametersList = new ArrayList<>();
        HashMap<String, String> processParameters = new HashMap<>();
        processParameters.put("assay", "12345");
        processParametersList.add(processParameters);
        int processID = rInstance.addToRun(runID, processParametersList).getFirst();
        assertEquals(1, rInstance.getRunSize(runID));
        assertFalse(rInstance.isCompletedRun(runID));
        //Check the contents of the process
        assertTrue(runID == pInstance.getParentRunID(processID));
        System.out.print("~ Done");

        System.out.println("Checking Process Contents..");
        ProcessingJob convertXMLtoJob = XMLJobInterpreter.getInstance().convertXMLtoJob(pInstance.getXMLForProcess(processID));
        assertEquals("12345", convertXMLtoJob.getProcessingParameters().get("assay"));
        System.out.print("~ Done");

        System.out.println("Checking Process Parameter Update..");
        processParameters.put("assay", "54321");
        pInstance.updateProcessParameters(processID, processParameters);
        convertXMLtoJob = XMLJobInterpreter.getInstance().convertXMLtoJob(pInstance.getXMLForProcess(processID));
        assertEquals("54321", convertXMLtoJob.getProcessingParameters().get("assay"));

        System.out.print("~ Done");

        System.out.println("Checking Process Failcount..");
        pInstance.increaseFailCount(processID);
        assertEquals(1, pInstance.getFailCount(processID));
        System.out.print("~ Done");

        System.out.println("Checking Process QueueCount...");
        assertEquals(1, pInstance.getUnqueuedProcesses(user).size());
        pInstance.setQueued(processID, true);
        assertTrue(pInstance.isQueued(processID));
        assertEquals(0, pInstance.getUnqueuedProcesses(user).size());
        System.out.print("~ Done");

        System.out.println("Checking Process State Update");
        String testUpdate = "I am a database test update";
        assertEquals(0, pInstance.getStepCount(processID));
        pInstance.updateProcessState(processID, testUpdate);
        assertEquals(testUpdate, pInstance.getProcessState(processID));
        assertEquals(1, pInstance.getStepCount(processID));
        System.out.print("~ Done");

        System.out.println("Checking Process Deletion...");
        pInstance.removeProcess(processID);
        assertEquals("Unknown", pInstance.getProcessState(processID));

        System.out.print("~ Done");

        System.out.println("Checking Run Deletion...");
        ArrayList<Integer> markedForDeletion = new ArrayList<>();
        markedForDeletion.add(runID);
        rInstance.deleteRun(markedForDeletion);
        assertEquals(0, rInstance.getRunSize(runID));
    }

}
