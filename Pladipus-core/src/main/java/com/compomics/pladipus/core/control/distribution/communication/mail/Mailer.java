/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.communication.mail;

import com.compomics.pladipus.core.control.distribution.service.UserService;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
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

     private Properties mailServerProperties;
    private Session getMailSession;
    private MimeMessage generateMailMessage;

    public void generateAndSendEmail(String subject, String message, String recipient) throws AddressException, MessagingException {
        // Step1
        System.out.println("\n 1st ===> setup Mail Server Properties..");
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        System.out.println("Mail Server Properties have been setup successfully..");
        // Step2
        System.out.println("\n\n 2nd ===> get Mail Session..");
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        generateMailMessage.setSubject(subject);
        String emailBody = message + "<br><br> Best regards, <br>Pladipus Admin";
        generateMailMessage.setContent(emailBody, "text/html");
        System.out.println("Mail Session has been created successfully..");
        // Step3
        System.out.println("\n\n 3rd ===> Get Session and Send mail");
        Transport transport = getMailSession.getTransport("smtp");
        transport.connect("smtp.gmail.com", "pladipus.compomics@gmail.com", UserService.getInstance().encryptPassword("justanotherpassword"));
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
    }

}
