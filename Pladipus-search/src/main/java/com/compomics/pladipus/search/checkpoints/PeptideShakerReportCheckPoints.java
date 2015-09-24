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
public enum PeptideShakerReportCheckPoints {

    test("test");
    private final String line;

    private final String feedback;

    private PeptideShakerReportCheckPoints(String line) {
        this.line = line;
        this.feedback = line;
    }

    private PeptideShakerReportCheckPoints(String line, String feedback) {
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
