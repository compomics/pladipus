/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.controller.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
     * Unzips a file to the specified folder
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
}
