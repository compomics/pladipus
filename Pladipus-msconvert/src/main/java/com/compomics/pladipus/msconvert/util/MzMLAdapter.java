/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.msconvert.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.Precursor;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;

/**
 *
 * @author Kenneth Verheggen <kenneth.verheggen@gmail.com>
 */
public class MzMLAdapter {
    
    private final MzMLControllerImpl dataAccessController;
    private final ArrayList<Comparable> spectrumIds;
    
    public MzMLAdapter(File inputFile) {
        this.dataAccessController = new MzMLControllerImpl(inputFile);
        this.spectrumIds = new ArrayList<>(dataAccessController.getSpectrumIds());
    }
    
    public void convertToMGF(File outputFile) {
        readSpectra(1,2);
    }
    
    private void readSpectra(int... MSlv) {
        List allowedLevels = Arrays.asList(MSlv);
        for (Comparable id : spectrumIds) {
            Spectrum spectrum = dataAccessController.getSpectrumById(id);
            //check the level
createMGFSpectrum(spectrum);
        }
    }
    
    private void createMGFSpectrum(Spectrum aSpectrum) {
        //get spectrum title
        String title = aSpectrum.getName();
        //get precursor mass?
        if (aSpectrum.getPrecursors() != null && !aSpectrum.getPrecursors().isEmpty()) {
            Precursor get = aSpectrum.getPrecursors().get(0);
            System.out.println(get);
        }
        //get spectrum level

    }
    
    public static void main(String[] args) throws MzMLUnmarshallerException {
        File input = new File("C:\\Users\\compomics\\Desktop\\Putty\\Putty\\OUT\\Putty\\B08-04098_c.mzML");
        MzMLAdapter adapter = new MzMLAdapter(input);
        adapter.convertToMGF(new File(input.getAbsolutePath() + ".mgf"));
    }
    
}
