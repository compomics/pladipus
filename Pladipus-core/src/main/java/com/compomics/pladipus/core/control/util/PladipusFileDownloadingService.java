package com.compomics.pladipus.core.control.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class PladipusFileDownloadingService {

    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(PladipusFileDownloadingService.class);
    /**
     * Downloads an entire folder locally
     * @param pathToFolder the path (http, ftp or file based) to the folder
     * @param destFolder the local folder
     * @return the filled destination folder
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File downloadFolder(String pathToFolder, File destFolder) throws IOException, URISyntaxException {
        destFolder.mkdirs();
        URL folderURL = new URL(getCorrectFilePath(pathToFolder));
        File localFolder;
        if (folderURL.getProtocol().contains("ftp")) {
            localFolder = downloadFolderFromFTP(folderURL, destFolder);
        } else {
            localFolder = downloadFolderFromLocalNetwork(folderURL, destFolder);
        }
        return localFolder;
    }
 /**
     * Downloads a file locally
     * @param pathToFolder the path (http, ftp or file based) to the file
     * @param destFolder the local folder
     * @return the downloaded file
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File downloadFile(String pathToFile, File destFolder) throws IOException {
        destFolder.mkdirs();
        URL fileURL = new URL(getCorrectFilePath(pathToFile));
        String fileName = fileURL.getFile().substring(fileURL.getFile().lastIndexOf("/"));
        File destFile = new File(destFolder, fileName);
        copy(fileURL, destFile);
        return destFile;
    }
/**
     * Downloads a file locally
     * @param pathToFolder the path (http, ftp or file based) to the file
     * @param destFolder the local folder
     * @param newFileName a new name for the downloaded file
     * @return the downloaded file
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File downloadFile(String pathToFile, File destFolder, String newFileName) throws IOException {
        destFolder.mkdirs();
        URL fileURL = new URL(getCorrectFilePath(pathToFile));
        File destFile = new File(destFolder, newFileName);
        copy(fileURL, destFile);
        return destFile;
    }

    private static File downloadFolderFromFTP(URL folderURL, File destFolder) throws IOException {
        destFolder = new File(destFolder, folderURL.getFile());
        destFolder.mkdirs();
        try {
            //new ftp client
            FTPClient ftp = new FTPClient();
            //try to connect
            ftp.connect(folderURL.getHost());
            //login to server
            if (!ftp.login("anonymous", "")) {
                ftp.logout();
            }
            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes. 
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
            }

            //enter passive mode
            ftp.enterLocalPassiveMode();
            //get system name
            System.out.println("Remote system is " + ftp.getSystemType());
            //change current directory
            ftp.changeWorkingDirectory(folderURL.getPath());
            System.out.println("Current directory is " + ftp.printWorkingDirectory());

            //get list of filenames
            FTPFile[] ftpFiles = ftp.listFiles();

            if (ftpFiles != null && ftpFiles.length > 0) {
                //loop thru files
                for (FTPFile file : ftpFiles) {
                    if (!file.isFile()) {
                        continue;
                    }
                    System.out.println("File is " + file.getName());
                    //get output stream
                    OutputStream output;
                    output = new FileOutputStream(new File(destFolder, file.getName()));
                    //get the file from the remote system
                    ftp.retrieveFile(file.getName(), output);
                    //close output stream
                    output.close();
                    //delete the file
                    // ftp.deleteFile(file.getName());
                }
            }
            ftp.logout();
            ftp.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return destFolder;
    }

    private static File downloadFolderFromLocalNetwork(URL folderURL, File destFolder) throws IOException, URISyntaxException {
        destFolder = new File(destFolder, folderURL.getFile().substring(folderURL.getFile().lastIndexOf("/")));
        destFolder.mkdirs();
        File networkDir = new File(folderURL.toURI());
        FileUtils.copyDirectory(networkDir, destFolder);
        return destFolder;
    }

    private static String getCorrectFilePath(String filePath) {
        if (!filePath.startsWith("ftp://") & !filePath.startsWith("http://")) {
            filePath = "file:///" + filePath;
        }
        return filePath;
    }

    private static void copy(URL fileURL, File destFile) throws FileNotFoundException, IOException {
        try (OutputStream os = new FileOutputStream(destFile);
                InputStream is = fileURL.openStream();) {
            byte[] b = new byte[2048];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            os.flush();
        }
    }

}
