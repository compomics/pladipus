package com.compomics.pladipus.core.control.engine.callback;

import com.compomics.pladipus.core.control.distribution.service.ProcessService;
import com.compomics.pladipus.core.model.feedback.Checkpoint;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class CallbackNotifier {

    /**
     * The a Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(CallbackNotifier.class);
    /**
     * The id of the process that's being monitored, if it is -1 nothing gets
     * notified
     */
    private final int processID;
    private final Collection<Checkpoint> checkpoints;

    public CallbackNotifier() {
        this.checkpoints = new HashSet<>();
        this.processID = -1;
    }

    public CallbackNotifier(int processID) {
        this.checkpoints = new HashSet<>();
        this.processID = processID;
    }

    public CallbackNotifier(Message message) throws JMSException {
        this.checkpoints = new HashSet<>();
        this.processID = Integer.parseInt(message.getJMSCorrelationID());
    }

    /**
     * Provides feedback to the queue where the task was pulled from.
     *
     * @param event the message to feedback
     * @param processID the unique id of the process
     * @throws JMSException
     */
    public void onNotification(String event,boolean increaseStepCounter) throws SQLException, JMSException {
        LOGGER.info(event);
        if (processID != -1) {
            ProcessService pService = ProcessService.getInstance();
            pService.updateProcessState(processID, event);
            if(increaseStepCounter){
                 pService.increaseProcessStepCount(processID);
            }
        }
    }

    public Collection<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public void addCheckpoints(Collection<Checkpoint> checkpoints) {
        this.checkpoints.addAll(checkpoints);
    }

     public void addCheckpoint(Checkpoint checkpoint) {
        this.checkpoints.add(checkpoint);
    }
}
