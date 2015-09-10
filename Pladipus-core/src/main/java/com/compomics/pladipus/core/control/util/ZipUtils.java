/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class ZipUtils {

    /**
     * the Logging instance
     */
    private static Logger LOGGER = Logger.getLogger(ZipUtils.class);
    /**
     * boolean indicating if the file is unzipped
     */
    private static boolean unzipped = false;
    /**
     * The zipping buffer size
     */
    private static int BUFFER_SIZE = 1024;

    /**
     * Unzips a file to the specified folder
     *
     * @param archive the zipped folder
     * @param outputDir the destination folder
     */
    public static void unzipArchive(File archive, File outputDir) {
        try (ZipFile zipfile = new ZipFile(archive)) {
            for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);
            }
            unzipped = true;
        } catch (Exception e) {
            LOGGER.error("Error while extracting file " + archive, e);
        } finally {
            if (unzipped) {
                archive.delete();
            }
        }
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        LOGGER.debug("Extracting: " + entry);

        try (BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry)); BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    private static void createDir(File dir) {
        LOGGER.debug("Creating dir " + dir.getName());
        if (!dir.mkdirs() & !dir.exists()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }
    /**
     * Unzips a single file to the specified folder
     *
     * @param input the original folder
     * @param output the destination zip file
     */
    public static void zipLargeFile(File input, File output) throws IOException {

        byte[] buffer = new byte[BUFFER_SIZE];
        output.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(output);
        try (ZipOutputStream zos = new ZipOutputStream(fos); FileInputStream in = new FileInputStream(input)) {
            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        }
    }

}
