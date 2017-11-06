/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search;

import com.compomics.pladipus.search.util.PrideAsapOutputExtractor;
import java.io.File;
/**
 *
 * @author Kenneth
 */
public class Playground {

    public static void main(String[] args) throws Exception {
        File inputPrideAsap = new File("C:\\Users\\compomics\\Desktop\\Experiment\\Pride_Extractor\\29079.pzip");
        PrideAsapOutputExtractor extractor = new PrideAsapOutputExtractor(inputPrideAsap, inputPrideAsap.getParentFile());
        System.out.println(extractor.getMgfFile().getAbsolutePath());
        System.out.println(extractor.getParameterFile().getAbsolutePath());
    }

}
