/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class SearchProperties {

    private static final File PROPERTY_FILE = new File(System.getProperty(("user.home")), ".compomics/pladipus_search/SearchProperties.properties");
    private static SearchProperties INSTANCE;
    private Properties searchProperties;

    public static SearchProperties getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new SearchProperties();
            } catch (IOException ex) {
                //get defaults.
                ex.printStackTrace();
            }
        }
        return INSTANCE;
    }

    private SearchProperties() throws IOException {
        searchProperties = new Properties();
        if (!PROPERTY_FILE.exists()) {
            //stuff that comes from the assay
            searchProperties.put("spectrum_files", "");
            searchProperties.put("fasta_file", "");
            searchProperties.put("id_params", "");
            searchProperties.put("output_folder", "");
            searchProperties.put("out_reports", "");

            //search settings that are not in the search parameters
            searchProperties.put("msgf", "0");
            searchProperties.put("omssa", "0");
            searchProperties.put("ms_amanda", "0");
            searchProperties.put("myrimatch", "0");
            searchProperties.put("comet", "0");
            searchProperties.put("tide", "0");
            searchProperties.put("andromeda", "0");
            searchProperties.put("xtandem", "1");

            //Peptideshaker command stuff
            searchProperties.put("experiment", "The experiment name");
            searchProperties.put("generate_reports", "true");
            searchProperties.put("reports", "0,1,2,3,4,5,6,7,8");
            searchProperties.put("psm_type", "0");
            searchProperties.put("save_cps", "1");
            //   processingParameters.put("time_out", "120");
            searchProperties.put("sample", "1");
            searchProperties.put("replicate", "1");
            PROPERTY_FILE.getParentFile().mkdirs();
            PROPERTY_FILE.createNewFile();
            try (FileWriter propertyWriter = new FileWriter(PROPERTY_FILE)) {
                searchProperties.store(propertyWriter, "");
            }
        }else{
            searchProperties.load(new FileInputStream(PROPERTY_FILE));
        }
    }

    private void loadParameterFromProperties(HashMap<String, String> processingParameters, String key) {
        processingParameters.put(key, searchProperties.getProperty(key));
    }

    public HashMap<String, String> getParameters() {
        HashMap<String, String> processingParameters = new HashMap<>();

        //stuff that comes from the assay
        loadParameterFromProperties(processingParameters, "spectrum_files");
        loadParameterFromProperties(processingParameters, "fasta_file");
        loadParameterFromProperties(processingParameters, "id_params");
        loadParameterFromProperties(processingParameters, "output_folder");
        loadParameterFromProperties(processingParameters, "out_reports");

        //search settings that are not in the search parameters
        loadParameterFromProperties(processingParameters, "msgf");
        loadParameterFromProperties(processingParameters, "omssa");
        loadParameterFromProperties(processingParameters, "ms_amanda");
        loadParameterFromProperties(processingParameters, "myrimatch");
        loadParameterFromProperties(processingParameters, "comet");
        loadParameterFromProperties(processingParameters, "tide");
        loadParameterFromProperties(processingParameters, "andromeda");
        loadParameterFromProperties(processingParameters, "xtandem");

        //Peptideshaker command stuff
        loadParameterFromProperties(processingParameters, "experiment");
        loadParameterFromProperties(processingParameters, "generate_reports");
        loadParameterFromProperties(processingParameters, "reports");
        loadParameterFromProperties(processingParameters, "psm_type");
        loadParameterFromProperties(processingParameters, "save_cps");
        // loadParameterFromProperties(processingParameters,"time_out", "120");
        loadParameterFromProperties(processingParameters, "sample");
        loadParameterFromProperties(processingParameters, "replicate");
        return processingParameters;
    }
}
