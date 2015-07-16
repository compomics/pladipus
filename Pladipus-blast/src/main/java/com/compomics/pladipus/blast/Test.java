/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.blast;

import java.util.HashMap;

/**
 *
 * @author Kenneth
 */
public class Test {

    public static void main(String[] args) throws Exception {
        HashMap<String, String> parametersForProcess = new HashMap<>();
        parametersForProcess.put("queryFasta", "C:\\Users\\Kenneth\\Desktop\\PladipusFTP\\data\\fasta\\HUMAN_Query.fasta");
        parametersForProcess.put("dbFasta", "C:\\Users\\Kenneth\\Desktop\\PladipusFTP\\data\\fasta\\HUMAN_concatenated_target_decoy.fasta");
        parametersForProcess.put("blastFolder", "C:\\Users\\Kenneth\\Downloads\\ncbi-blast-2.2.31+-x64-win64\\ncbi-blast-2.2.31+\\bin");
        parametersForProcess.put("blastType", "prot");
        parametersForProcess.put("output", "C:\\Users\\Kenneth\\Desktop\\PladipusFTP\\results");

        BlastSetupStep blastSetupStep = new BlastSetupStep();
        blastSetupStep.setParameters(parametersForProcess);
        blastSetupStep.doAction();

        BlastPrepareDbStep blastMakeDbStep = new BlastPrepareDbStep();
        blastMakeDbStep.setParameters(parametersForProcess);
        blastMakeDbStep.doAction();

        BlastPStep blastPStep = new BlastPStep();
        blastPStep.setParameters(parametersForProcess);
        blastPStep.doAction();

    }
}
