/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.queue.impl;

import com.compomics.pladipus.core.control.distribution.service.queue.CompomicsConsumer;
import com.compomics.pladipus.core.control.engine.ProcessingEngine;
import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class CompomicsDurableConsumer extends CompomicsConsumer {
/**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CompomicsDurableConsumer.class);

    public CompomicsDurableConsumer(CompomicsQueue queue) throws JMSException, IOException {
        super(queue);
    }

    @Override
    public void processMessage(Message message) throws JMSException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        if (message instanceof TextMessage) {
            try {
                Future<Boolean> future = executor.submit(new ProcessingEngine(message));
                //executor.shutdown();
                //executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
                if (future.get(1, TimeUnit.DAYS)) {
                    LOGGER.info("Succesfully completed task");
                }
            } catch (JMSException | IOException | InterruptedException | ExecutionException | TimeoutException ex) {
                //otherwise other executionexceptions are ignored...
                if (ex instanceof RejectedExecutionException) {
                    LOGGER.info("Machine is not qualified to run this job");
                }
            }
        }
    }

}
