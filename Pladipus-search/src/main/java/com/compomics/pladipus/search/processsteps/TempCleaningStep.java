/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.search.processsteps;

import com.compomics.pladipus.core.model.processing.ProcessingStep;
import java.io.File;
import java.io.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth Verheggen
 */
public class TempCleaningStep extends ProcessingStep {

    public TempCleaningStep() {

    }

    @Override
    public boolean doAction() throws Exception {

        System.out.println("Running " + this.getClass().getName());
        //clean all the file-extensions that are not in the list to be saved
        File temp = new File(parameters.get("temp"));

        File[] files = temp.listFiles(new FileFilter() {
            private final FileNameExtensionFilter filter
                    = new FileNameExtensionFilter("Result Files", "cps");

            @Override
            public boolean accept(File file) {
                return (!file.isDirectory() && filter.accept(file));
            }
        });

        File outputFolder = new File(parameters.get("outputfolder"));
        outputFolder.mkdirs();
        for (File aFile : files) {
            File dest = new File(outputFolder, aFile.getName());
            if (aFile.isDirectory()) {
                FileUtils.copyDirectory(aFile, dest, true);
            } else {
                FileUtils.copyFile(aFile, dest, true);
            }
        }
        FileUtils.deleteDirectory(temp);
        return true;
    }

    @Override
    public String getDescription() {
        return "Saving results...";
    }

}
