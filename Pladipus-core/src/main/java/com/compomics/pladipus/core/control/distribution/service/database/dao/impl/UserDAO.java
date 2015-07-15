/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.database.dao.impl;

import com.compomics.pladipus.core.control.distribution.service.database.AutoCloseableDBConnection;
import com.compomics.pladipus.core.control.distribution.service.database.dao.PladipusDAO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class UserDAO extends PladipusDAO implements AutoCloseable {
/**
     * The Logging instance
     */
    public static final Logger LOGGER = Logger.getLogger(UserDAO.class);
    /**
     * The DAO instance
     */
    private static UserDAO pladipusJobDAO;

    /**
     *
     * @return the DAO instance
     */
    public static UserDAO getInstance() {
        if (pladipusJobDAO == null) {
            pladipusJobDAO = new UserDAO();
        }
        return pladipusJobDAO;
    }

    private UserDAO() {

    }

    // USER RELATED METHODS
    /**
     *
     * @param user the users login name
     * @param passWord the byte representation of the password
     * @param contactAddress a valid e-mail address
     * @return the success of creating
     * @throws SQLException
     */
    public boolean createUser(String user, String passWord, String contactAddress) throws SQLException {
        boolean created = false;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement queryUser = c.prepareStatement("SELECT USER_NAME FROM USERS WHERE USER_NAME=?");
                PreparedStatement updateUser = c.prepareStatement("INSERT INTO USERS(USER_NAME,PASSWORD,CONTACT) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
                PreparedStatement updateUserRole = c.prepareStatement("INSERT INTO USER_ROLES(USER_ID,ROLE_ID) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
            
            queryUser.setString(1, user);

            if (!queryUser.executeQuery().next()) {
                updateUser.setString(1, user);
                updateUser.setString(2, passWord);
                updateUser.setString(3, contactAddress);
                updateUser.executeUpdate();
                ResultSet rs = updateUser.getGeneratedKeys();
                if (rs.next()) {
                    updateUserRole.setInt(1, rs.getInt(1));
                    updateUserRole.setInt(2, 2);
                    created = true;
                } else {
                    created = false;
                }
                c.commit();
            } else {
                created = false;
            }
        }
        return created;
    }

    /**
     *
     * @param user the users login name
     * @param user_role_id the new ID for the user's role
     * @throws SQLException
     */
    public void setUserRole(String user, int user_role_id) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement selectUser = c.prepareStatement("SELECT USER_ID FROM USERS WHERE USER_NAME=?");
                PreparedStatement updateUser = c.prepareStatement("UPDATE USER_ROLES SET ROLE_ID=? WHERE USER_ID=?")) {
            
            selectUser.setString(1, user);
            ResultSet rs = selectUser.executeQuery();
            if (rs.next()) {
                updateUser.setInt(1, user_role_id);
                updateUser.setInt(2, rs.getInt("USER_ID"));
                updateUser.executeUpdate();
            }
            c.commit();
        }
    }

    /**
     *
     * @param user to be queried
     * @return the provided contact e-mail for said user
     * @throws SQLException
     */
    public String getUserContactAddress(String user) throws SQLException {
        String contactInfo = null;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement selectUser = c.prepareStatement("SELECT CONTACT FROM USERS WHERE USER_NAME=?")) {
            
            selectUser.setString(1, user);
            try (ResultSet executeQuery = selectUser.executeQuery()) {
                if (executeQuery.next()) {
                    contactInfo = executeQuery.getString("CONTACT");
                }
            }
        }
        return contactInfo;
    }

    /**
     *
     * @param user to be queried
     * @return the password as byte format for said user
     * @throws SQLException
     */
    public String getUserPassWord(String user) throws SQLException {
        String passWord = null;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement selectUser = c.prepareStatement("SELECT PASSWORD FROM USERS WHERE USER_NAME=?")) {
            
            selectUser.setString(1, user);
            try (ResultSet executeQuery = selectUser.executeQuery()) {
                if (executeQuery.next()) {
                    passWord = executeQuery.getString("PASSWORD");
                }
            }
        }
        return passWord;
    }

    /**
     *
     * @return all available user roles
     * @throws SQLException
     */
    public HashMap<Integer, String> getUserRoles() throws SQLException {
        HashMap<Integer, String> roles = new HashMap<>();
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT DISTINCT ROLE_ID,ROLE FROM ROLES"); ResultSet executeQuery = retrieveStatement.executeQuery()) {
            
            while (executeQuery.next()) {
                roles.put(executeQuery.getInt("ROLE_ID"), executeQuery.getString("ROLE"));
            }
        }
        return roles;
    }

    /**
     *
     * @param user the username
     * @return the role id that was bestowed upon this user
     * @throws SQLException
     */
    public int getUserRole(String user) throws SQLException {
        int userRole = -1;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(); PreparedStatement retrieveStatement = c.prepareStatement("SELECT ROLE_ID FROM USERS INNER JOIN USER_ROLES WHERE USERS.USER_NAME=?")) {
            
            retrieveStatement.setString(1, user);
            ResultSet executeQuery = retrieveStatement.executeQuery();
            if (executeQuery.next()) {
                userRole = executeQuery.getInt("ROLE_ID");
            }
        }
        return userRole;
    }

    /**
     *
     * @param user the user to update a password for
     * @param encryptPassword the encrypted password
     * @throws SQLException
     */
    public void updatePassword(String user, String encryptPassword) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateUser = c.prepareStatement("UPDATE USERS SET PASSWORD=? WHERE USER_NAME=?")) {
            
            updateUser.setString(1, encryptPassword);
            updateUser.setString(2, user);
            updateUser.executeUpdate();
            c.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param user the username that needs an update
     * @param newEmail a new e-mail address
     * @throws SQLException
     */
    public void updateContactInfo(String user, String newEmail) throws SQLException {
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection(false);
                PreparedStatement updateUser = c.prepareStatement("UPDATE USERS SET CONTACT=? WHERE USER_NAME=?")) {
            
            updateUser.setString(1, newEmail);
            updateUser.setString(2, user);
            updateUser.executeUpdate();
            c.commit();
        }
    }

    /**
     *
     * @param user the users login name
     * @return
     * @throws SQLException
     */
    public boolean userExists(String user) throws SQLException {
        boolean exists = true;
        try (AutoCloseableDBConnection c = new AutoCloseableDBConnection();
                PreparedStatement selectUser = c.prepareStatement("SELECT USER_ID FROM USERS WHERE USERS.USER_NAME=?");) {
            
            selectUser.setString(1, user);
            ResultSet rs = selectUser.executeQuery();
            exists = rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

}
