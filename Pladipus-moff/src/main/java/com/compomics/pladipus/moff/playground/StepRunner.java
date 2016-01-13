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
        String rawFolder = ("C:\\Users\\compomics\\Desktop\\MOFF\\RAW");
        String inputFolder = ("C:\\Users\\compomics\\Desktop\\MOFF\\Input");
        String tol = "10";
        
        if(args!=null&&args.length>1){
            rawFolder = args[0];
            inputFolder=args[1];
            tol = args[2];
        }
 
        String raw = "";
        for (File aRaw : new File(rawFolder).listFiles()) {
            raw += aRaw.getAbsolutePath() + ",";
        }
        String input = "";
        for (File anInput : new File(inputFolder).listFiles()) {
            input += anInput.getAbsolutePath() + ",";
        }
        parameters.put("ps_report", input);
        parameters.put("raw", raw);
        parameters.put("-tol", tol);
        //other parameters     
        MoffStep step = new MoffStep();
        step.setParameters(parameters);

        step.doAction();

    }

}
