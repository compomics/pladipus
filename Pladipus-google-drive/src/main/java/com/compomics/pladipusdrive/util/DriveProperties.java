/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipusdrive.util;

import com.compomics.pladipusdrive.auth.DriveServiceManager;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author JavaSaurus <javasaurusdev@gmail.com>
 */
public class DriveProperties {

    private static final Logger LOGGER = Logger.getLogger(DriveProperties.class);
    private static DriveProperties INSTANCE;

    Properties properties;
    File propertiesFile;

    public static DriveProperties getInstance() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new DriveProperties();
        }
        return INSTANCE;
    }

    private DriveProperties() throws IOException {
        properties = new Properties();
        propertiesFile = new File(DriveServiceManager.getInstance().getDATA_STORE_DIR().getParentFile(), "drive.properties");
        if (propertiesFile.exists()) {
            try {
                properties.load(new FileReader(propertiesFile));
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        } else {
            initProperties();
        }
    }

    private void initProperties() throws IOException {
        properties.put("drive_storage_folder_name", HostNameResolver.getHostName());
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            properties.store(writer, "");
        }
    }

    public String GetDriveStorageFolderName() {
        return properties.getProperty("drive_storage_folder_name");
    }

    public void SetDriveStorageFolderName(String name) {
        properties.put("drive_storage_folder_name", name);
    }

    public String GetDriveStorageFolderId() {
        return properties.getProperty("drive_storage_folder_id");
    }

    public void SetDriveStorageFolderId(String id) {
        properties.put("drive_storage_folder_id", id);
    }

}
