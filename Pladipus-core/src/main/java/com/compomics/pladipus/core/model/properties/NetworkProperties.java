package com.compomics.pladipus.core.model.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Kenneth Verheggen
 */
public class NetworkProperties extends PladipusProperties {

    /**
     * The properties Instance
     */
    private static NetworkProperties instance;
    /**
     * The Logger Instance
     */
    private static final Logger LOGGER = Logger.getLogger(NetworkProperties.class);
    /**
     * The location of the configuration folder
     */
    private static File configFolder;

    private NetworkProperties() {

    }

    private static File getConfigFolder() throws URISyntaxException {
        return new File(new File(NetworkProperties.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile(), "config");
    }

    /**
     *
     * @return an initiated instance of the network properties
     */
    public static NetworkProperties getInstance() {
        if (instance == null) {
            instance = new NetworkProperties();
            try {
                configFolder = getConfigFolder();
            } catch (URISyntaxException ex) {
                LOGGER.warn("Could not find property file, changing to defaults");
                configFolder = new File(System.getProperty("user.home"));
            }
            defaultPropFile = new File(configFolder, "network.properties");
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
            defaultPropFile.getParentFile().mkdirs();
            try {
                defaultPropFile.createNewFile();
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
            try (OutputStream outputStream = new FileOutputStream(defaultPropFile); InputStream inputStream = new ClassPathResource("network_default.properties").getInputStream()) {
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }
    }

    /**
     * Load a specified file into the properties
     *
     * @param propertiesFile
     * @throws IOException
     */
    public void load(File propertiesFile) throws IOException {
        instance.clear();
        instance.load(new FileInputStream(propertiesFile));
    }

    /**
     * Reload the properties
     *
     * @throws IOException
     */
    public void reload() throws IOException {
        instance.clear();
        instance.load(new FileInputStream(defaultPropFile));
    }

    /**
     * Save a specified file into the properties
     *
     * @throws IOException
     */
    public void save() throws IOException {
        instance.store(new FileWriter(defaultPropFile), "");
    }

    /**
     *
     * @return The host the activeMQ instance is running on
     */
    public String getActiveMQLocation() {
        String host = instance.getProperty("AMQ.host");
        String port = instance.getProperty("AMQ.port.queue");
        return "tcp://" + host + ":" + port;
    }

    /**
     *
     * @return The host the activeMQ instance is running JMX on
     */
    //"service:jmx:rmi:///jndi/rmi://157.193.229.78:1099/jmxrmi
    public String getActiveMQJMXLocation() {
        String host = instance.getProperty("AMQ.host");
        String port = instance.getProperty("AMQ.port.jmx");
        return "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
    }

    /**
     *
     * @return the host the pladipus MYSQL is running on
     */
    public String getStateDatabaseLocation() {
        String host = instance.getProperty("db.host");
        String port = instance.getProperty("db.port");
        return host + ":" + port + "/pladipus";
    }

    /**
     *
     * @return the login for the state database
     */
    public String getStateDatabaseLogin() {
        return instance.getProperty("db.login");
    }

    /**
     *
     * @return the login for the state database
     */
    public String getStateDatabasePassWord() {
        return instance.getProperty("db.pass");
    }

    /**
     *
     * @return the level of logging
     */
    public String getLoggingLevel() {
        return instance.getProperty("log.level");
    }

    /**
     *
     * @return the maximal amount of times a job can be re-ran before being
     * labelled as "failed"
     */
    public int getMaxFailCount() {
        return Integer.parseInt(instance.getProperty("AMQ.max.retry"));
    }

    /**
     *
     * @return the location of the classpath
     */
    public String getAdditionalClasspath() {
        return instance.getProperty("app.classpath");
    }

    /**
     *
     * @return the location of the installed properties file
     */
    public String getFileLocation() {
        return defaultPropFile.getAbsolutePath();
    }

}
