/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.controller.setup;

import com.compomics.pladipus.controller.util.ActiveMQPropertyUpdater;
import com.compomics.pladipus.core.control.util.PladipusFileDownloadingService;
import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.properties.NetworkProperties;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class InstallActiveMQ {

    /**
     * the local location of the activeMQ folder
     */
    private static final File activeMQFolder = new File(System.getProperty("user.home") + "/.compomics/pladipus/activeMQ/");

    public InstallActiveMQ() {

    }

    /**
     *
     * @param host the activeMQ host name
     * @param amqPort the port to connect to activeMQ
     * @param jmxPort the port to listen to connect and query JMX
     * @return success
     * @throws IOException
     */
    public boolean setupActiveMQ(String host, String amqPort, String jmxPort) throws IOException {
        boolean success = false;
        //1.
        downloadActiveMQ();
        //2. 
        updateProperties(host, amqPort, jmxPort);
        //3.
        writeWorkerBash();
        return success;
    }

    private static void downloadActiveMQ() throws IOException {
        String link = "http://apache.cu.be/activemq/5.11.1/apache-activemq-5.11.1-bin.zip";
        File downloadFile = PladipusFileDownloadingService.downloadFile(link, activeMQFolder);
        ZipUtils.unzipArchive(downloadFile, activeMQFolder);
    }

    /**
     * Updates the activeMQ properties in the local propertyfile
     *
     * @param host the activeMQ host name
     * @param amqPort the port to connect to activeMQ
     * @param jmxPort the port to listen to connect and query JMX
     * @throws IOException
     */
    public void updateProperties(String host, String amqPort, String jmxPort) throws IOException {
        NetworkProperties properties = NetworkProperties.getInstance();
        properties.setProperty("AMQ.host", host);
        properties.setProperty("AMQ.port.queue", amqPort);
        properties.setProperty("AMQ.version", "5.11.1");
        properties.setProperty("app.classpath", System.getProperty("user.home") + "/.compomics/pladipus/external/");
        properties.save();
        URL inputUrl = new InstallActiveMQ().getClass().getResource("/doc/activemq.xml");
        File conf = findFile(activeMQFolder, "conf");
        File activeMQSettingsFile = new File(conf, "activemq.xml");
        FileUtils.copyURLToFile(inputUrl, activeMQSettingsFile);
        //update properties in the activeMQ file
        ActiveMQPropertyUpdater.updateActiveMQProperties(activeMQSettingsFile, host, amqPort, jmxPort);


    }

    private static File findFile(File root, String fileName) {
        File configFile = null;
        File[] list = root.listFiles();
        for (File f : list) {
            if (f.getName().equals(fileName)) {
                configFile = f;
            } else if (f.isDirectory()) {
                configFile = findFile(f, fileName);
            }
            if (configFile != null) {
                break;
            }
        }
        return configFile;
    }

    /**
     * Writes an executable script to the desktop
     * @throws IOException
     */
    public void writeWorkerBash() throws IOException {
        String os = System.getProperty("os.name");
        String jarLocation = findFile(activeMQFolder, "activemq.jar").getAbsolutePath();
        String command = "java -jar " + jarLocation + " start";
        FileWriter bashWriter;
        if (os.toLowerCase().contains("windows")) {
            File bashFile = new File(System.getProperty("user.home") + "/Desktop/Pladipus-ActiveMQ.bat");
            bashWriter = new FileWriter(bashFile);
        } else {
            File bashFile = new File(System.getProperty("user.home") + "/Desktop/Pladipus-ActiveMQ.sh");
            bashWriter = new FileWriter(bashFile);
        }
        bashWriter.append(command).flush();
        bashWriter.close();
    }

}
