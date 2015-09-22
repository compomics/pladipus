/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.controller.setup;

import com.compomics.pladipus.core.model.properties.NetworkProperties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.jdbc.ScriptRunner;

/**
 *
 * @author Kenneth Verheggen
 */
public class InitMySQL {

    public InitMySQL() {

    }

    public boolean dropPladipus(Connection connection) throws SQLException {
        int executeQuery = connection.createStatement().executeUpdate("drop schema pladipus;");
        return executeQuery == 1;
    }

    public boolean pladipusExists(Connection connection) throws SQLException {
        ResultSet executeQuery = connection.createStatement().executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'pladipus'");
        return executeQuery.next();
    }

    /**
     * Installs the pladipus database on the given connection. Note,you need
     * create rights for this on the server !
     *
     * @param connection the connection to mysql
     * @throws IOException
     */
    public void setupMySql(Connection connection) throws IOException {
        //2. import from SQL script?
        ScriptRunner runner = new ScriptRunner(connection);
        runner.setAutoCommit(true);
        //runner.setLogWriter(null);
        //runner.setErrorLogWriter(null);
        runner.setStopOnError(false);
        //
        File tempFile = File.createTempFile("init_script", ".sql");
        tempFile.deleteOnExit();
        try (OutputStream out = new FileOutputStream(tempFile); InputStream in = getClass().getClassLoader().getResource("doc/PLADIPUS_INIT_SCRIPT.sql").openStream()) {
            IOUtils.copy(in, out);
            runner.runScript(new FileReader(tempFile));
        }

    }

    /**
     * Updates the local properties file
     *
     * @param host the mysql host
     * @param port the mysql port
     * @param login the mysql login
     * @return success
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean updateProperties(String host, String port, String login, String password) throws FileNotFoundException, IOException {
        NetworkProperties properties = NetworkProperties.getInstance();
        properties.setProperty("db.host", host);
        properties.setProperty("db.port", port);
        properties.setProperty("db.login", login);
        properties.setProperty("db.pass", password);
        properties.save();
        return true;
    }

    /**
     * Updates the local properties file
     *
     * @param host the mysql host
     * @param port the mysql port
     * @param login the mysql login
     * @return success
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean updateProperties(String host, String port, String login) throws FileNotFoundException, IOException {
        return updateProperties(host, port, login, "");
    }
}
