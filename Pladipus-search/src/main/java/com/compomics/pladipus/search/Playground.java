/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search;

import com.compomics.pladipus.search.processsteps.PeptideShakerStep;
import com.compomics.pladipus.search.processsteps.SearchGUIStep;
import com.compomics.pladipus.search.processsteps.SearchSetupStep;
import java.util.HashMap;

/**
 *
 * @author Kenneth
 */
public class Playground {

    public static void main(String[] args) throws Exception {

        HashMap<String, String> parameters = new HashMap<>();
        //searchGUI param
        //     parameters.put("spectrum_files", "C:\\Users\\compomics\\Desktop\\COMPLETE\\55746\\55746\\55746.mgf");
        parameters.put("fasta_file", "C:\\Users\\compomics\\Desktop\\COMPLETE\\mus_musculus_12_2015.fasta");
        parameters.put("input", "C:\\Users\\compomics\\Desktop\\1636\\1636.zip");
        // parameters.put("id_params", "C:\\Users\\compomics\\Desktop\\COMPLETE\\55746\\55746\\55746.par");
        parameters.put("output_folder", "C:/Users/Kenneth/Documents/Output/searches_test");
        //turn off all search engines?
        parameters.put("xtandem", "0");
        parameters.put("msfg", "1");
        parameters.put("omssa", "0");
        parameters.put("ms_amanda", "0");
        parameters.put("myrimatch", "0");
        parameters.put("comet", "0");
        parameters.put("tide", "0");
        parameters.put("andromeda", "0");

        //peptideShaker param
        parameters.put("experiment", "Peptide_Shaker_Test");
        parameters.put("out", "C:/Users/Kenneth/Documents/Output/test.cps");
        parameters.put("sample", "PeptideShaker_Pladipus");
        parameters.put("replicate", "1");

        /*  SearchInputRetrievalStep step0 = new SearchInputRetrievalStep();
        step0.setParameters(parameters);
        step0.doAction();*/
        SearchSetupStep step1 = new SearchSetupStep();
        step1.setParameters(parameters);
        step1.doAction();

        SearchGUIStep step2 = new SearchGUIStep();
        step2.setParameters(parameters);
        step2.doAction();

        PeptideShakerStep step3 = new PeptideShakerStep();
        step3.setParameters(parameters);
        step3.doAction();

    }

}
