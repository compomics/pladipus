/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipusdrive.auth;

import com.compomics.pladipusdrive.upload.DriveInitializer;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author JavaSaurus <javasaurusdev@gmail.com>
 */
public class DriveServiceManager {

    private static final Logger LOGGER = Logger.getLogger(DriveServiceManager.class);
    private static DriveServiceManager INSTANCE;

    private Credential credential;
    private Drive drive;
    /**
     * Application name.
     */
    private String APPLICATION_NAME = "PladipusDrive";

    /**
     * Directory to store user credentials for this application.
     */
    private java.io.File DATA_STORE_DIR;

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private JsonFactory JSON_FACTORY;

    /**
     * Global instance of the HTTP transport.
     */
    private HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials at
     * ~/.credentials/drive-java-quickstart
     */
    private List<String> SCOPES = new ArrayList<>();

    private void connect() throws IOException, GeneralSecurityException {
        SCOPES.addAll(DriveScopes.all());
        JSON_FACTORY = JacksonFactory.getDefaultInstance();
        DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/drive-java-pladipus");
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
    }

    private DriveServiceManager() {
        try {
            connect();
            credential = authorize();
            drive = getDriveService();
        } catch (IOException | GeneralSecurityException t) {
            LOGGER.error(t);
        }
    }

    public static DriveServiceManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DriveServiceManager();
        }
        return INSTANCE;
    }

    public String getAPPLICATION_NAME() {
        return APPLICATION_NAME;
    }

    public File getDATA_STORE_DIR() {
        return DATA_STORE_DIR;
    }

    public FileDataStoreFactory getDATA_STORE_FACTORY() {
        return DATA_STORE_FACTORY;
    }

    public HttpTransport getHTTP_TRANSPORT() {
        return HTTP_TRANSPORT;
    }

    public List<String> getSCOPES() {
        return SCOPES;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public Drive getDrive() {
        return drive;
    }

    public void setDrive(Drive drive) {
        this.drive = drive;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    private Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in
                = DriveServiceManager.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow
                = new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Drive client service.
     *
     * @return an authorized Drive client service
     * @throws IOException
     */
    private Drive getDriveService() throws IOException {
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

}
