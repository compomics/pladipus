/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipusdrive;

import com.compomics.pladipusdrive.auth.DriveServiceManager;
import com.compomics.pladipusdrive.upload.DriveInitializer;
import com.compomics.pladipusdrive.upload.DriveOperations;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author JavaSaurus <javasaurusdev@gmail.com>
 */
public class PlayGround {

    private static final Logger LOGGER = Logger.getLogger(PlayGround.class);

    public static void main(String[] args) {
        java.io.File testingFile = new java.io.File("F:/work/1K_Report_Sample/Human_8222_0_Extended_PSM_Report.txt");
        try {
          
            DriveOperations instance = new DriveOperations();
            new DriveInitializer().init();
            System.out.println("TEST id is " + instance.getFolderId("TEST"));
            instance.UploadReport(testingFile);
        } catch (IOException ex) {
            LOGGER.error(ex);
       }
    }
}
