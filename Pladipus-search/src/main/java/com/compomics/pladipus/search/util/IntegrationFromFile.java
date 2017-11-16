/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.util;

import com.compomics.pladipus.core.control.util.ZipUtils;
import com.compomics.pladipus.core.model.exception.PladipusProcessingException;
import com.compomics.pladipus.core.model.exception.UnspecifiedPladipusException;
import com.compomics.pladipus.search.processsteps.PeptideShakerStep;
import com.compomics.pladipus.search.processsteps.SearchGUIStep;
import com.compomics.pladipus.search.processsteps.SearchSetupStep;
import com.compomics.pladipus.search.processsteps.WebServiceSearchSetupStep;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class IntegrationFromFile {

    public static final Logger LOGGER = Logger.getLogger(IntegrationFromFile.class);

    public static void run(String spectrum_files, String id_params, String fasta, File outputFolder, boolean peptideShaker) {

        HashMap<String, String> parameters = SearchProperties.getInstance().getParameters();
        try {
            parameters.put("spectrum_files", spectrum_files);
            parameters.put("id_params", id_params);
            parameters.put("fasta_file", fasta);
            parameters.put("output_folder", outputFolder.getAbsolutePath());
            File reports = new File(outputFolder, "reports");
            reports.mkdirs();
            parameters.put("out_reports", reports.getAbsolutePath());

            //setup
            SearchSetupStep setupStep = new SearchSetupStep();
            setupStep.setParameters(parameters);
            //search
            SearchGUIStep searchStep = new SearchGUIStep();
            searchStep.setParameters(parameters);
            //peptideShaker

            PeptideShakerStep pepStep = new PeptideShakerStep();
            pepStep.setParameters(parameters);

            //do the steps in tandem?
            setupStep.doAction();
            searchStep.doAction();
            if (peptideShaker) {
                pepStep.doAction();
            }
        } catch (PladipusProcessingException | UnspecifiedPladipusException ex) {
            ex.printStackTrace();
        } finally {

            File reportFolder = new File(parameters.get("out_reports"));
            if (reportFolder.exists()) {
                LOGGER.info("Zipping peptideshaker reports...");
                try {
                    File zippedFolder = new File(reportFolder.getAbsolutePath() + ".zip");
                    ZipUtils.zipFolder(reportFolder, zippedFolder);
                    for (File aFile : reportFolder.listFiles()) {
                        aFile.deleteOnExit();
                    }
                    if (zippedFolder.exists()) {
                        FileUtils.deleteQuietly(reportFolder);
                    }
                } catch (UnspecifiedPladipusException | IOException ex) {
                    LOGGER.error(ex);
                }
            }
        }

    }

}
