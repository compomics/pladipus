/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipusdrive.upload;

import com.compomics.pladipusdrive.auth.DriveServiceManager;
import com.compomics.pladipusdrive.util.DriveProperties;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.IOException;
import java.util.Collections;

/**
 *
 * @author JavaSaurus <javasaurusdev@gmail.com>
 */
public class DriveOperations {

    public DriveOperations() {

    }

    public void UploadReport(String resultFolderName, java.io.File filePath) throws IOException {
        String resultFolderId = CreateFolder(resultFolderName, DriveProperties.getInstance().GetDriveStorageFolderId());
        UploadReport(filePath, resultFolderId);
    }

    public File UploadReport(java.io.File filePath) throws IOException {
        return UploadReport(filePath, DriveProperties.getInstance().GetDriveStorageFolderId());
    }

    public File UploadReport(java.io.File filePath, String folderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(filePath.getName());
        fileMetadata.setParents(Collections.singletonList(folderId));
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
        FileContent mediaContent = new FileContent("text/csv", filePath);
        File file = DriveServiceManager.getInstance().getDrive().files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
        return file;
    }

    public String CreateFolder(String folderName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        File file = DriveServiceManager.getInstance().getDrive().files().create(fileMetadata)
                .setFields("id")
                .execute();
        return file.getId();
    }

    public String CreateFolder(String folderName, String parentFolderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setParents(Collections.singletonList(parentFolderId));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        File file = DriveServiceManager.getInstance().getDrive().files().create(fileMetadata)
                .setFields("id, parents")
                .execute();
        return file.getId();
    }

    public String CreateFolderInParentName(String folderName, String parentFolderName) throws IOException {
        return CreateFolder(folderName, getFolderId(parentFolderName));
    }

    public String getFolderId(String folderName) throws IOException {
        String pageToken = "";
        while (pageToken != null) {
            FileList result = DriveServiceManager.getInstance().getDrive().files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                if (file.getName().equals(folderName)) {
                    return file.getId();
                }
            }
            pageToken = result.getNextPageToken();
        }
        return "";
    }

}
