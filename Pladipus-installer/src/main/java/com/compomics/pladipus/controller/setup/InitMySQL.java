/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.controller.setup;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.jdbc.ScriptRunner;

/**
 * class to setup the mysql part of pladipus
 *
 * @author Kenneth Verheggen
 */
public class InitMySQL {

    //todo remove hardcoding of scheme name

    /**
     * drops the pladipus schema
     *
     * @param connection connection to the database to drop the scheme from
     * @return true if success
     * @throws SQLException
     */
    public boolean dropPladipus(Connection connection) throws SQLException {
        try (PreparedStatement stat = connection.prepareStatement("drop schema pladipus;")) {
            return stat.execute();
        }
    }

    /**
     * checks if the pladipus schema exists
     *
     * @param connection the connection to the database to check on
     * @return true if success
     * @throws SQLException
     */
    public boolean pladipusExists(Connection connection) throws SQLException {

        try (PreparedStatement stat = connection.prepareStatement("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'pladipus'")) {
            try (ResultSet set = stat.executeQuery()) {
                return (!set.isAfterLast() && !set.isBeforeFirst());
            }
        }
    }

    /**
     * Installs the pladipus database on the given connection. Note,you need
     * create rights for this on the server !
     *
     * @param connection the connection to mysql
     * @throws IOException
     */
    public boolean setupMySql(Connection connection) throws IOException {

        //don't quite like this runner thing
        //2. import from SQL script?
        ScriptRunner runner = new ScriptRunner(connection);
        runner.setAutoCommit(true);
        runner.setStopOnError(true);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("doc/PLADIPUS_INIT_SCRIPT.sql")) {
            if (in != null) {
                runner.runScript(new InputStreamReader(in));
                runner.closeConnection();
                return true;
            } else {
                throw new FileNotFoundException("could not find sql script to import");
            }
        }
    }
}
