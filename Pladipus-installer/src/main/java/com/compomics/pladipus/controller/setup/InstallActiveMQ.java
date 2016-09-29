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

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.util.Zip4jUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.URI;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class InstallActiveMQ {

    //todo remove hardcoding of folders

    /**
     * the local location of the activeMQ folder
     */
    private File activeMQFolder = new File(System.getProperty("user.home") + "/pladipus/activeMQ/");

    String link = "http://www.apache.org/dyn/closer.cgi?filename=/activemq/5.14.0/apache-activemq-5.14.0-bin.zip&action=download";


    /**
     *
     * @param host the activeMQ host name
     * @param amqPort the port to connect to activeMQ
     * @param jmxPort the port to listen to connect and query JMX
     * @return success
     * @throws IOException
     */
    public boolean setupActiveMQ(String host, String amqPort, String jmxPort) throws IOException, ZipException {
        boolean success = false;
        //1.
        downloadActiveMQ();
        //2. 
        updateProperties(host, amqPort, jmxPort);
        //3.
        writeWorkerBash();
        return success;
    }

    private void downloadActiveMQ() throws IOException, ZipException {


        //File downloadFile = PladipusFileDownloadingService.downloadFile(link, activeMQFolder);

        if (!activeMQFolder.exists() & !activeMQFolder.mkdirs()) {
            throw new IOException("could not make install folder");
        }

        URL website = new URL(link);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        Path downloadFile = Files.createTempFile("activemqdownload",null);
        FileOutputStream fos = new FileOutputStream(downloadFile.toFile());

        if (fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE) != 0) {
            if (Objects.equals(DigestUtils.md5Hex(new FileInputStream(downloadFile.toFile())), "4b844f588672e6616bd6f006253d6148")) {
                ZipFile zipFile = new ZipFile(downloadFile.toFile());
                zipFile.extractAll(activeMQFolder.getPath());
            } else {
                throw new IOException("md5 digest did not match, aborting");
            }
        }
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
        properties.setProperty("app.classpath", System.getProperty("user.home") + "/pladipus/external/");
        properties.save();
        URL inputUrl = new InstallActiveMQ().getClass().getResource("/doc/activemq.xml");
        File conf = findFile(activeMQFolder, "conf");
        File activeMQSettingsFile = new File(conf, "activemq.xml");
        FileUtils.copyURLToFile(inputUrl, activeMQSettingsFile);
        //update properties in the activeMQ file
        ActiveMQPropertyUpdater.updateActiveMQProperties(activeMQSettingsFile, host, amqPort, jmxPort);


    }

    public void setActiveMQFolder(File activeMQFolder) {
        this.activeMQFolder = activeMQFolder;
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
