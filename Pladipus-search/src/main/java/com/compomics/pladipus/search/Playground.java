/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search;

import com.compomics.pladipus.core.model.processing.ProcessingJob;
import com.compomics.pladipus.core.model.processing.ProcessingRun;
import com.compomics.pladipus.core.model.processing.ProcessingStep;
import com.compomics.pladipus.search.processsteps.PeptideShakerStep;
import com.compomics.pladipus.search.processsteps.SearchGUIStep;
import com.compomics.pladipus.search.processsteps.SearchSetupStep;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Kenneth
 */
public class Playground {

    public static void main(String[] args) throws Exception {
        String user = "Kenneth";
        HashMap<String, String> parameters = new HashMap<>();
        //searchGUI param
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

        ProcessingJob job = new ProcessingJob(parameters, user);
        job.add(new SearchSetupStep());
        job.add(new SearchGUIStep());
        job.add(new PeptideShakerStep());

        LinkedHashMap<String, ProcessingJob> jobs = new LinkedHashMap<>();
        jobs.put("test_search", job);

        ProcessingRun run = new ProcessingRun(jobs, "My_First_Run");

        for (Map.Entry<String, ProcessingJob> aJob : run.getProcesses().entrySet()) {
            System.out.println("Running " + aJob.getKey());
            if (aJob.getValue().allowRun()) {
                for (ProcessingStep aStep : aJob.getValue()) {
                    System.out.println(aStep.getDescription());
                    aStep.doAction();
                }
            }
        }

       
        
    }

}
