/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.util;

import static com.compomics.pladipus.core.control.util.ZipUtils.zipFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
        LOGGER.info("Extracting " + prideasapZip.getAbsolutePath() + " to " + outputFolder.getAbsolutePath());
        try (ZipFile input = new ZipFile(prideasapZip)) {
            Enumeration<? extends ZipEntry> entries = input.entries();
            ZipEntry mgfEntry = null;
            ZipEntry paramEntry = null;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".par")) {
                    paramEntry = entry;
                } else if (entry.getName().endsWith(".spectra.zip") | entry.getName().endsWith(".mgf.zip") | entry.getName().endsWith(".mgf")) {
                    mgfEntry = entry;
                }
            }
            if (paramEntry != null && mgfEntry != null) {
                File param = new File(outputFolder, new File(input.getName().replace(".pzip", ".par")).getName());
                extractParameters(param, input, paramEntry);
                File mgf = new File(outputFolder, new File(input.getName().replace(".pzip", ".mgf")).getName());
                extractMGF(mgf, input, mgfEntry);
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }

    }

    private File extractFile(File outputFile, ZipFile input, ZipEntry entry) throws IOException {

        try (BufferedReader stream = new BufferedReader(new InputStreamReader(input.getInputStream(entry)));
                FileWriter out = new FileWriter(outputFile);) {
            String line;
            while ((line = stream.readLine()) != null) {
                out.append(line + System.lineSeparator()).flush();
            }
            out.flush();

        }
        return outputFile;
    }

    private void extractParameters(File outputFile, ZipFile input, ZipEntry paramEntry) throws IOException {

        parameterFile = extractFile(outputFile, input, paramEntry);
    }

    private void extractMGF(File outputFile, ZipFile input, ZipEntry mgfEntry) throws IOException {
        if (mgfEntry.getName().endsWith(".mgf")) {
            mgfFile = extractFile(outputFile, input, mgfEntry);
        } else {
            try (ZipInputStream innerZip = new ZipInputStream(input.getInputStream(mgfEntry))) {
                ZipEntry mgfZipEntry = null;
                byte[] buffer = new byte[2048];
                while ((mgfZipEntry = innerZip.getNextEntry()) != null) {
                    if (mgfZipEntry.getName().endsWith(".mgf") | mgfZipEntry.getName().endsWith(".mgf.zip")) {
                        try (FileOutputStream output = new FileOutputStream(outputFile)) {
                            int len;
                            while ((len = innerZip.read(buffer)) > 0) {
                                output.write(buffer, 0, len);
                            }
                        }
                    }
                }
                mgfFile = outputFile;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public File getParameterFile() {
        return parameterFile;
    }

    public File getMgfFile() {
        return mgfFile;
    }

}
