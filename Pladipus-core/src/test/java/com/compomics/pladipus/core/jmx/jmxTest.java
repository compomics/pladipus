/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.jmx;

import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsProducer;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.DeleteOperation;
import com.compomics.pladipus.core.control.distribution.service.queue.jmx.operation.impl.QueryOperation;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import junit.framework.TestCase;

/**
 *
 * @author Kenneth Verheggen
 */
public class jmxTest extends TestCase {

    public jmxTest(String testName) {
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
     * Test of getEnqueued method, of class QueryOperation.
     */
    public void testQueryPurge() throws Exception {
        int processID = 1231245;

        QueryOperation queryInstance = new QueryOperation();
        DeleteOperation deleteInstance = new DeleteOperation();
        //pre-purge the queue
        deleteInstance.purgeQueue(CompomicsQueue.JOB);
        //launch a mock job
        System.out.println("Sending a task to the queue...");
        CompomicsProducer producer2 = new CompomicsProducer(CompomicsQueue.JOB, "TEST2", processID);
         Thread producerThread2 = new Thread(producer2, "ProducerThread2");
        producerThread2.start();

        //check the job is on the queue
        assertEquals(true,queryInstance.isOnQueue(CompomicsQueue.JOB, processID));

        //check the queue size is correct
        long expResult = 1L;
        long result = queryInstance.getCurrentQueueSize(CompomicsQueue.JOB);
        assertEquals(expResult, result);

        //check the queue gets emptied
        int delete = deleteInstance.deleteJobFromQueue(CompomicsQueue.JOB, processID);
        expResult = 1L;
        assertEquals(expResult, delete);
        //check the process is not on the queue
        assertEquals(queryInstance.isOnQueue(CompomicsQueue.JOB, processID), false);

    }

}
