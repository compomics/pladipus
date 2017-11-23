/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipusdrive.upload;

import com.compomics.pladipusdrive.util.DriveProperties;
import com.compomics.pladipusdrive.util.HostNameResolver;
import java.io.IOException;

/**
 *
 * @author JavaSaurus <javasaurusdev@gmail.com>
 */
public class DriveInitializer {

    private String processingFolder = "PladipusProcessing";
    private DriveOperations driveOperations;

    public void init() throws IOException {
        //check if the main parent exists...
        driveOperations = new DriveOperations();
        String parentProcessingFolderID = driveOperations.getFolderId(processingFolder);
        if (parentProcessingFolderID.isEmpty()) {
            driveOperations.CreateFolder(processingFolder);
        }
        //check if this machine's working folder exists. if it doesn't, create it
        String myProcessingFolder = "plad_"+HostNameResolver.getHostName();
        String processingFolderId = driveOperations.getFolderId(myProcessingFolder);
        if (processingFolderId.isEmpty()) {
            processingFolderId = driveOperations.CreateFolder(myProcessingFolder, parentProcessingFolderID);
        }
        //store these properties in the appropriate location
        DriveProperties properties = DriveProperties.getInstance();
        properties.SetDriveStorageFolderName(myProcessingFolder);
        properties.SetDriveStorageFolderId(processingFolderId);
    }

}
