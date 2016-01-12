/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.moff.playground;

import com.compomics.pladipus.moff.logic.PSOutputParser;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author compomics
 */
public class ParserRunner {

    public static void main(String[] args) throws IOException {
        File[] files = new File("C:\\Users\\compomics\\Desktop\\MOFF").listFiles();
        for(File aFile:files){
        File outputFile = new File(aFile.getAbsolutePath().replace(".txt", ".converted.txt"));
        PSOutputParser.convert(aFile,outputFile);
           
        }
     }

}
