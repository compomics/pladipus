/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.checkpoints;

/**
 *
 * @author Kenneth
 */
public enum PeptideShakerCheckPoints {

    GENERATE_PROTEIN_TREE("See http://compomics.github.io/compomics-utilities/wiki/proteininference.html.","Generating protein tree"),
    READING_INPUT("Reading identification files"),
    MAPPING_PEPTIDES("Mapping peptides to proteins"),
    IMPORTING_PSMS("Importing PSMs"),
    COMPUTING_PROBABILITIES("Computing assumptions probabilities"),
    SCORING_PTMS("Scoring PTMs"),
    RESOLVING_PEPTIDE_INFERENCE("Resolving peptide inference"),
    BUILDING_PEPTIDES_AND_PROTEINS("Building Peptides and Proteins"),
    ATTACHING_PEPTIDE_PROBABILITIES("Attaching Peptide Probabilities"),
    GENERATING_PROTEIN_MAP("Generating protein map"),
    RESOLVING_PROTEIN_INFERENCE("Resolving protein inference"),
    FINDING_FDR_THRESHOLDS("Finding FDR Thresholds"),
    SCORING_PTM_PEPTIDES("Scoring PTMs in peptides"),
    SCORING_PTM_PROTEINS("Scoring PTMs in proteins"),
    SAVING_RESULTS("Saving results");
    
    
    ;
    private final String line;
    private final String feedback;

    private PeptideShakerCheckPoints(String line) {
        this.line = line;
        this.feedback = line;
    }

    private PeptideShakerCheckPoints(String line, String feedback) {
        this.line = line;
        this.feedback = feedback;
    }

    public String getLine() {
        return line;
    }

    public String getFeedback() {
        return feedback;
    }
}
