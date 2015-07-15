/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.prerequisite;

import com.compomics.pladipus.core.model.prerequisite.Prerequisite;
import com.compomics.pladipus.core.model.prerequisite.PrerequisiteParameter;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author Kenneth Verheggen
 */
public class PrerequisiteTest extends TestCase {

    public PrerequisiteTest(String testName) {
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
     * Test of addPrerequisite method, of class Prerequisite.
     */
    public void testPrerequisiteTrue() throws IOException {
        System.out.println("Test correct prerequisites");
        PrerequisiteParameter parameter = PrerequisiteParameter.CORES;
        parameter.getCustomOptionValue(parameter, String.valueOf(Runtime.getRuntime().availableProcessors()));

        Prerequisite instance = new Prerequisite();

        instance.addPrerequisite(parameter);
        
        if (System.getProperty("os.arch").toLowerCase().contains("64")) {
            instance.addPrerequisite(PrerequisiteParameter.ARCH_64);
        } else {
            instance.addPrerequisite(PrerequisiteParameter.ARCH_32);
        }
        
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            instance.addPrerequisite(PrerequisiteParameter.OS_WINDOWS);
        } else {
            instance.addPrerequisite(PrerequisiteParameter.OS_LINUX);
        }      
        
        assertEquals(true, instance.checkPreRequisites());
    }

    /**
     * Test of addPrerequisite method, of class Prerequisite.
     */
    public void testPrerequisiteFalse() throws IOException {
     System.out.println("Test false prerequisites");
        PrerequisiteParameter parameter = PrerequisiteParameter.CORES;
        parameter.getCustomOptionValue(parameter, String.valueOf(-1));

        Prerequisite instance = new Prerequisite();

        instance.addPrerequisite(parameter);
        
        if (!System.getProperty("os.arch").toLowerCase().contains("64")) {
            instance.addPrerequisite(PrerequisiteParameter.ARCH_64);
        } else {
            instance.addPrerequisite(PrerequisiteParameter.ARCH_32);
        }
        
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            instance.addPrerequisite(PrerequisiteParameter.OS_WINDOWS);
        } else {
            instance.addPrerequisite(PrerequisiteParameter.OS_LINUX);
        }      
        
        assertEquals(false, instance.checkPreRequisites());
    }

}
