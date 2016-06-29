package com.compomics.pladipus.core.control.distribution.service.queue;

import com.compomics.pladipus.core.model.queue.CompomicsQueue;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.jms.JMSException;
import javax.swing.JProgressBar;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class CompomicsGUIProducer extends CompomicsProducer {

    /**
     * The progressBar to update
     */
    private ProgressDialogX progressDialog;

    public CompomicsGUIProducer(CompomicsQueue queue, int priority, ProgressDialogX progressDialog) throws IOException, JMSException {
        super(queue, priority);
        this.progressDialog = progressDialog;
    }

    public CompomicsGUIProducer(CompomicsQueue queue, ProgressDialogX progressDialog) throws IOException, JMSException {
        super(queue);
        this.progressDialog = progressDialog;
    }

    @Override
    protected void processMessage(Entry<String,Integer> message) throws JMSException, SQLException {
        super.processMessage(message);
        progressDialog.increaseSecondaryProgressCounter();
    }

    @Override
    public void run() {
        progressDialog.setMaxSecondaryProgressCounter(messageProcessIds.size());
        progressDialog.setSecondaryProgressCounter(0);
        super.run();
    }

}
