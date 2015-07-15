/*
 * Copyright 2014 Kenneth.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 */
package com.compomics.pladipus.core.control.distribution.service;

import com.compomics.pladipus.core.control.distribution.service.database.dao.impl.UserDAO;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import org.jasypt.util.password.StrongPasswordEncryptor;

/**
 *
 * @author Kenneth Verheggen
 */
public class UserService {
  /**
     * The a password encryptor
     */
    private final StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
  /**
     * The userservcie instance
     */
    private static UserService userService;

    /**
     *
     * @return the service instance
     */
    public static UserService getInstance() {
        if (userService == null) {
            userService = new UserService();
        }
        return userService;
    }

    private UserService() {

    }

    private String encryptPassword(String password) {
        return encryptor.encryptPassword(password);
    }

    /**
     *
     * @param user the username
     * @param password the non-encrypted password for the user
     * @param contact the e-mail address for the user
     * @return wether the user was created
     * @throws SQLException
     * @throws UnsupportedEncodingException
     */
    public boolean registerNewUser(String user, String password, String contact) throws SQLException, UnsupportedEncodingException, IOException {
        boolean created = false;
        try (UserDAO dao = UserDAO.getInstance()) {
            created = dao.createUser(user, encryptPassword(password), contact);
        }
        return created;
    }

    /**
     *
     * @param user
     * @param password the user password
     * @return boolean indicating whether the password matches the encrypted
     * entry in the database
     * @throws SQLException
     * @throws UnsupportedEncodingException
     */
    public boolean verifyUser(String user, String password) throws SQLException, UnsupportedEncodingException {
        try (UserDAO dao = UserDAO.getInstance()) {
            String storedPassword = dao.getUserPassWord(user);
            return isCorrectPassword(password, storedPassword);
        }
    }

    private boolean isCorrectPassword(String givenPassword, String storedPassword) {
        return encryptor.checkPassword(givenPassword, storedPassword);
    }

    /**
     *
     * @param username
     * @return the contact address for the given user
     * @throws SQLException
     */
    public String getUserContactAddress(String username) throws SQLException {
        try (UserDAO dao = UserDAO.getInstance()) {
            return dao.getUserContactAddress(username);
        }
    }

    /**
     *
     * @param username
     * @return the password for the user (encrypted)
     * @throws SQLException
     */
    public String getUserPassWord(String username) throws SQLException {
        try (UserDAO dao = UserDAO.getInstance()) {
            return dao.getUserPassWord(username);
        }
    }

    /**
     *
     * @return the available user roles
     * @throws SQLException
     */
    public HashMap<Integer, String> getUserRoles() throws SQLException {
        try (UserDAO dao = UserDAO.getInstance()) {
            return dao.getUserRoles();
        }
    }

    /**
     *
     * @param loggedInUser
     * @return the user role ID for the given user
     * @throws SQLException
     */
    public int getUserRoles(String loggedInUser) throws SQLException {
        try (UserDAO dao = UserDAO.getInstance()) {
            return dao.getUserRole(loggedInUser);
        }
    }

    /**
     * Sets a new userRole key for a user
     * @param username
     * @param userRoleKey
     * @throws SQLException
     */
    public void setUserRole(String username, int userRoleKey) throws SQLException {
        try (UserDAO dao = UserDAO.getInstance()) {
            dao.setUserRole(username, userRoleKey);
        }
    }

    /**
     * Sets a new password for a given user
     * @param user
     * @param newPass
     * @throws SQLException
     */
    public void updatePassword(String user, String newPass) throws SQLException {
        try (UserDAO dao = UserDAO.getInstance()) {
            dao.updatePassword(user, encryptPassword(newPass));
        }
    }

    /**
     * Updates the contact address for the user
     * @param user
     * @param newEMail
     * @throws SQLException
     */
    public void updateContactInfo(String user, String newEMail) throws SQLException {
        try (UserDAO dao = UserDAO.getInstance()) {
            dao.updateContactInfo(user, newEMail);
        }
    }

    /**
     * Check if the user exists
     * @param user
     * @return a boolean indicating the user is to be found in the database
     * @throws SQLException
     */
    public boolean userExists(String user) throws SQLException {
        try (UserDAO dao = UserDAO.getInstance()) {
            return dao.userExists(user);
        }
    }

}
