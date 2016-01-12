/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.moff.playground;

import com.compomics.pladipus.moff.steps.MoffStep;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author compomics
 */
public class StepRunner {

    public static void main(String[] args) throws Exception {
        HashMap<String, String> parameters = new HashMap<>();
        //the input files
        File rawFolder = new File("C:\\Users\\compomics\\Desktop\\MOFF\\RAW");
        File inputFolder = new File("C:\\Users\\compomics\\Desktop\\MOFF\\Input");
        String raw = "";
        for (File aRaw : rawFolder.listFiles()) {
            raw += aRaw.getAbsolutePath() + ",";
        }
        String input = "";
        for (File anInput : inputFolder.listFiles()) {
            input += anInput.getAbsolutePath() + ",";
        }
        parameters.put("ps_report", input);
        parameters.put("raw", raw);

        //other parameters
        
        
        
        MoffStep step = new MoffStep();
        step.setParameters(parameters);

        step.doAction();

    }

}
