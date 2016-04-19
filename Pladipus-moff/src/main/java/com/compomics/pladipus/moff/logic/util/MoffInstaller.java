package com.compomics.moff.gui.control.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.log4j.Logger;

/**
 * This class manages the installation of the MoFF script
 *
 * @author Kenneth Verheggen
 */
public class MoffInstaller {

    private static final Logger LOGGER = Logger.getLogger(MoffInstaller.class);

    public static void installMoff() throws IOException, URISyntaxException {
        //install in the current jar directory
        installMoff(getMoFFFolder());
    }

    public static void installMoff(File destFile) throws IOException, URISyntaxException {
        if (!destFile.exists()) {
            unzipResource(destFile, "moff.zip");
            /*   destFile = new File(System.getProperty("user.home") + "/pladipus/tools/moff/example");
            unzipResource(destFile, "example.zip");*/
        }
    }

    public static File getMoFFFolder() {
        try {
            File moffFolder = new File(getCurrentJarDirectory(), "moff");
            return moffFolder;
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(ex);
        }
        //return a default folder?
        return new File(System.getProperty("user.home") + "/pladipus/tools/moff/");
    }

    private static void unzipResource(File destFile, String resourceName) throws IOException, URISyntaxException {
        InputStream stream = MoffInstaller.class.getClassLoader().getResourceAsStream(resourceName);
        unzipMoff(stream, destFile);

    }

    private static File getCurrentJarDirectory() throws UnsupportedEncodingException {
        String path = MoffInstaller.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(path, "UTF-8");
        return new File(decodedPath).getParentFile();
    }

    /**
     * Unzip it
     *
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    private static void unzipMoff(InputStream inputStream, File outputFolder) throws IOException {
        byte[] buffer = new byte[1024];
        //get the zipped file list entry
        try ( //get the zip file content
                ZipInputStream zis = new ZipInputStream(inputStream)) {
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        installMoff();
    }

}
