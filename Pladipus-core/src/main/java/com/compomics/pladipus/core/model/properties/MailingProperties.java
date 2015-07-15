/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Kenneth Verheggen
 */
public class MailingProperties extends PladipusProperties {

    /**
     * The properties Instance
     */
    private static MailingProperties instance;
    /**
     * The Logger Instance
     */
    private static final Logger LOGGER = Logger.getLogger(MailingProperties.class);
    /**
     * The authenticator to connect to SMTP servers
     */
    private Authenticator authenticator;

    private MailingProperties() {
    }

    /**
     *
     * @return an initiated mailing properties instance
     */
    public static MailingProperties getInstance() {
        if (instance == null) {
            instance = new MailingProperties();
            defaultPropFile = new File(System.getProperty("user.home") + "/.compomics/pladipus/config/mail.properties");
            if (!defaultPropFile.exists()) {
                copyFromResources();
            }
            try {
                instance.load(defaultPropFile);
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }
        return instance;
    }

    private static void copyFromResources() {
        //loads the codontable from within the jar...
        if (defaultPropFile != null && !defaultPropFile.exists()) {
            try {
                defaultPropFile.getParentFile().mkdirs();
                defaultPropFile.createNewFile();
                InputStream inputStream = new ClassPathResource("mail_default.properties").getInputStream();
                OutputStream outputStream = new FileOutputStream(defaultPropFile);
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }
    }

    /**
     * Loads the mailing properties
     * @param propertiesFile
     * @throws IOException
     */
    public void load(File propertiesFile) throws IOException {
        instance.clear();
        instance.load(new FileInputStream(propertiesFile));
    }

    /**
     *
     * @return the mailing protocol
     */
    public String getProtocol() {
        return instance.getProperty("mail.protocol");
    }

    /**
     *
     * @return wether debug statements should be printed to the console
     */
    public boolean isDebugMessagesRequired() {
        String property = instance.getProperty("mail.debug.messages");
        return property != null && property.equalsIgnoreCase("true");
    }

    /**
     *
     * @return the port the e-mail server can be contacted through
     */
    public int getPort() {
        return Integer.parseInt(instance.getProperty("mail.port"));
    }

    /**
     *
     * @return the hostname or IP of the e-mail server
     */
    public String getHost() {
        return instance.getProperty("mail.host");
    }

    /**
     *
     * @return the masked mailing address
     */
    public String getMaskedaddress() {
        return instance.getProperty("mail.masked.address");
    }

    /**
     *
     * @return the real mailing address
     */
    public String getRealaddress() {
        return instance.getProperty("mail.real.address");
    }

    /**
     *
     * @return the password required to log in the mailing account
     */
    public String getPassword() {
        return instance.getProperty("mail.real.password");
    }

    /**
     *
     * @return wether authentication is mandatory
     */
    public boolean isAuthenticateRequired() {
        return instance.getProperty("mail.authenticate").equalsIgnoreCase("true");
    }

    private void initMailingProperties() {
        put("mail.smtp.host", getHost());
        put("mail.smtp.port", getPort());
        switch (getProtocol().toUpperCase()) {
            case "SMTPS":
                put("mail.smtp.ssl.enable", true);
                break;
            case "TLS":
                put("mail.smtp.starttls.enable", true);
                break;
            case "SMTP":
                if (isAuthenticateRequired()) {
                    put("mail.smtp.auth", true);
                    authenticator = new Authenticator() {

                        private final PasswordAuthentication pa = new PasswordAuthentication(getRealaddress(), getPassword());

                        @Override
                        public PasswordAuthentication getPasswordAuthentication() {
                            return pa;
                        }
                    };
                    break;
                }
        }
    }

    /**
     *
     * @return an authenticator object to verify credentials
     */
    public Authenticator getMailingAuthenticator() {
        return authenticator;
    }

}
