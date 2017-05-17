/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.util;

import static com.compomics.pladipus.core.control.util.ZipUtils.zipFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class PrideAsapOutputExtractor {

    private static final Logger LOGGER = Logger.getLogger(PrideAsapOutputExtractor.class);

    private final File prideasapZip;
    private final File outputFolder;
    private File parameterFile;
    private File mgfFile;

    public PrideAsapOutputExtractor(File prideasapZip, File outputFolder) {
        this.prideasapZip = prideasapZip;
        this.outputFolder = outputFolder;
        extract();
    }

    private void extract() {
        try (ZipFile input = new ZipFile(prideasapZip)) {
            Enumeration<? extends ZipEntry> entries = input.entries();
            ZipEntry mgfEntry = null;
            ZipEntry paramEntry = null;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".par")) {
                    paramEntry = entry;
                } else if (entry.getName().endsWith(".mgf.zip")) {
                    mgfEntry = entry;
                }
            }
            if (paramEntry != null && mgfEntry != null) {

                extractParameters(input, paramEntry);
                extractMGF(input, mgfEntry);
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }

    }

    private void extractParameters(ZipFile input, ZipEntry paramEntry) throws IOException {
        File outputFile = new File(outputFolder, input.getName().replace(".pzip", ".par"));
        try (BufferedReader stream = new BufferedReader(new InputStreamReader(input.getInputStream(paramEntry)));
                FileWriter out = new FileWriter(outputFile);) {
            String line;
            while ((line = stream.readLine()) != null) {
                out.append(line + System.lineSeparator()).flush();
            }
            out.flush();
            parameterFile = outputFile;
        }

    }

    private void extractMGF(ZipFile input, ZipEntry mgfEntry) throws IOException {
        try (ZipInputStream innerZip = new ZipInputStream(input.getInputStream(mgfEntry))) {
            File outputFile = new File(outputFolder, input.getName().replace(".pzip", ".mgf"));
            ZipEntry mgfZipEntry = null;
            while ((mgfZipEntry = innerZip.getNextEntry()) != null) {
                if (mgfZipEntry.getName().endsWith(".mgf")) {
                    try (BufferedReader stream = new BufferedReader(new InputStreamReader(input.getInputStream(mgfZipEntry)));
                            FileWriter out = new FileWriter(outputFile);) {
                        String line;
                        while ((line = stream.readLine()) != null) {
                            out.append(line + System.lineSeparator()).flush();
                        }
                        out.flush();
                    }
                }
            }
            mgfFile = outputFile;
        }
    }

    public File getParameterFile() {
        return parameterFile;
    }

    public File getMgfFile() {
        return mgfFile;
    }

}
