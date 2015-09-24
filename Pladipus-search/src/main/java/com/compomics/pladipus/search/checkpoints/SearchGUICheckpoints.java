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
public enum SearchGUICheckpoints {

    REINDEXING("Reindexing"),
    VALIDATING_MGF("Validating MGF file"),
    SEARCH_ENGINE_MSGF("ms-gf+ command:", "Running MS-GF+"),
    SEARCH_ENGINE_ANDROMEDA("andromeda command:", "Running Andromeda"),
    SEARCH_ENGINE_COMET("comet command:", "Running Comet"),
    SEARCH_ENGINE_MYRIMATCH("myrimatch command:", "Running Myrimatch"),
    SEARCH_ENGINE_MSAMANDA("ms amanda command:", "Running MSAmanda"),
    SEARCH_ENGINE_OMSSA("omssa command:", "Running OMSSA"),
    SEARCH_ENGINE_XTANDEM("xtandem command:", "Running XTandem"),
    SEARCH_ENGINE_TIDE_INDEX("tide index command:", "Running TIDE : indexing"),
    SEARCH_ENGINE_TIDE_SEARCH("tide search command:", "Running TIDE : searching"),
    PROCESSING_MSCONVERT("msconvert command:", "Running MSConvert"),
    PROCESSING_MAKEBLASTDB("makeblastdb command:", "Running makeblastdb"),
    PROCESSING_PEPTIDESHAKER("PeptideShaker", "Running PeptideShaker"),
    PROCESSING_RESULTS("Zipping output files");
    private final String line;
    private final String feedback;

    private SearchGUICheckpoints(String line) {
        this.line = line;
        this.feedback = line;
    }

    private SearchGUICheckpoints(String line, String feedback) {
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
