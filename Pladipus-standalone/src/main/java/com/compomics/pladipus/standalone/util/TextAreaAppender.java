package com.compomics.pladipus.standalone.util;

import java.util.Date;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author compomics
 */
public class TextAreaAppender extends WriterAppender {

    private final JTextPane logTextArea;

    public TextAreaAppender(JTextPane logTextArea) {
        this.logTextArea = logTextArea;
        this.logTextArea.setContentType("text/html");
    }

    @Override
    public void append(LoggingEvent loggingEvent) {
        if (loggingEvent.getRenderedMessage().startsWith("e@")) {
            appendString("\t" + loggingEvent.getRenderedMessage() + System.lineSeparator());
        } else {
            appendString(new Date(loggingEvent.getTimeStamp()) + "\t" + loggingEvent.getRenderedMessage() + System.lineSeparator());
        }

    }

    private void appendString(String s) {

        try {
            Document doc = logTextArea.getDocument();
            doc.insertString(doc.getLength(), s, null);
        } catch (BadLocationException exc) {
            exc.printStackTrace();
        }
    }
}

