package com.compomics.pladipus.moff.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author compomics
 */
public class MoffInstaller {

    public static void installMoff() throws IOException, URISyntaxException {
        File destFile = new File(System.getProperty("user.home") + "/pladipus/tools/moff");
        installMoff(destFile);
    }

    public static void installMoff(File destFile) throws IOException, URISyntaxException {
        if (!destFile.exists()) {
            unzipResource(destFile, "moff.zip");
            destFile = new File(System.getProperty("user.home") + "/pladipus/tools/moff/example");
            unzipResource(destFile, "example.zip");
        }
    }

    private static void unzipResource(File destFile, String resourceName) throws IOException, URISyntaxException {
        InputStream stream = MoffInstaller.class.getClassLoader().getResourceAsStream(resourceName);
        unZipIt(stream, destFile);

    }

    /**
     * Unzip it
     *
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    private static void unZipIt(InputStream inputStream, File outputFolder) {

        byte[] buffer = new byte[1024];

        try {

            //get the zip file content
            ZipInputStream zis
                    = new ZipInputStream(inputStream);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
