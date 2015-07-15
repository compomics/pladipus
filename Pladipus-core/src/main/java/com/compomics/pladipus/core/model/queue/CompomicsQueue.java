/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.queue;

/**
 *
 * @author Kenneth Verheggen
 */
public enum CompomicsQueue {

    JOB("pladipus.job", false), UPDATE("pladipus.update", true);
    /**
     * The name of the queue
     */
    private final String queueName;
    /**
     * Boolean indicating if the queue is a broadcaster queue that pushes tasks
     * to all workers persistently. For example : system update jobs
     */
    private final boolean broadcastToAll;

    private CompomicsQueue(String queueName, boolean broadcastToAll) {
        this.queueName = queueName;
        this.broadcastToAll = broadcastToAll;
    }

    public String getQueueName() {
        return queueName;
    }

    public boolean isBroadcastToAll() {
        return broadcastToAll;
    }

}
