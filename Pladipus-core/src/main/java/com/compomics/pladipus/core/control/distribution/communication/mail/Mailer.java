/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.communication.mail;

import com.compomics.pladipus.core.model.properties.MailingProperties;
import java.io.IOException;
import java.util.Date;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class Mailer {

    /**
     * The Recieving instance
     */
    private final String recipient;
    /**
     * The Logging instance
     */
    private final static Logger LOGGER = Logger.getLogger(Mailer.class);

    /**
     *
     * @param recipient the fully qualified e-mail address for the reciever
     */
    public Mailer(String recipient) {
        this.recipient = recipient;
    }

    /**
     *
     * @param subject e-mail title
     * @param message e-mail body
     * @throws IOException
     */
    public void sendMail(String subject, String message) throws IOException {
        MailingProperties mailProps = MailingProperties.getInstance();
        Session session = Session.getInstance(mailProps, mailProps.getMailingAuthenticator());
        session.setDebug(mailProps.isDebugMessagesRequired());
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            LOGGER.info("Sending as " + mailProps.getMaskedaddress() + " to " + recipient);
            mimeMessage.setFrom(new InternetAddress(mailProps.getMaskedaddress()));
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            mimeMessage.setSubject(subject);
            mimeMessage.setSentDate(new Date());
            mimeMessage.setText(message);
            Transport.send(mimeMessage);
            LOGGER.info("Done !");
        } catch (MessagingException ex) {
            LOGGER.error(ex);
        }
    }

}
