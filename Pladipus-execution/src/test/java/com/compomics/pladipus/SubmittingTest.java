/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus;

import com.compomics.pladipus.core.control.distribution.PladipusTrafficManager;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author Kenneth Verheggen
 */
public class SubmittingTest extends TestCase {

    private PladipusTrafficManager trafficManager = PladipusTrafficManager.getInstance();
    
    public SubmittingTest(String testName) {
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
     * Test of pushToPladipus method, of class CLIExecutor.
     */
    public void testPushToPladipus() throws Exception {
        System.out.println("pushToPladipus");

        File testXML = new File("C:\\Users\\Kenneth\\Documents\\Projects\\Pladipus\\Pladipus-execution\\src\\test\\resources\\test_template.xml");
        System.out.println(testXML.exists());
        File testTSV = new File("C:\\Users\\Kenneth\\Documents\\Projects\\Pladipus\\Pladipus-execution\\src\\test\\resources\\test_config.tsv");
        System.out.println(testTSV.exists());
        trafficManager.pushToPladipus(testXML, testTSV);
    }

}
