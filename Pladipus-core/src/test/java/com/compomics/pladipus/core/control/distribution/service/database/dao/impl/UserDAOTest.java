/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.control.distribution.service.database.dao.impl;

import com.compomics.pladipus.core.database.MockDatabaseTest;
import java.util.HashMap;

/**
 *
 * @author Kenneth Verheggen
 */
public class UserDAOTest extends MockDatabaseTest {

    public UserDAOTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private final UserDAO instance = UserDAO.getInstance();

    /**
     * Test of createUser method, of class UserDAO.
     */
    public void testCreateUser() throws Exception {
        System.out.println("createUser");
        String user = "pladmin2";
        String passWord = "pladmin2";
        String contactAddress = "pladmin@ugent.be";
        assertTrue(instance.createUser(user, passWord, contactAddress));
        assertTrue(instance.userExists(user));
    }

    /**
     * Test of setUserRole method, of class UserDAO.
     */
    public void testSetUserRole() throws Exception {
        System.out.println("setUserRole");
        String user = "pladmin";
        int user_role_id = 2;
        instance.setUserRole(user, user_role_id);
        int userRole = instance.getUserRole(user);
        assertEquals(2,userRole);
    }

    /**
     * Test of getUserRoles method, of class UserDAO.
     */
    public void testGetUserRoles() throws Exception {
        System.out.println("getUserRoles");
        HashMap<Integer, String> result = instance.getUserRoles();
        assertEquals(2, result.size());
    }

    /**
     * Test of updatePassword method, of class UserDAO.
     */
    public void testUpdatePassword() throws Exception {
        System.out.println("updatePassword");
        String user = "pladmin";
        String expResult = "pladmin2";
        instance.updatePassword(user, expResult);
        assertEquals(expResult, instance.getUserPassWord(user));
    }

    /**
     * Test of updateContactInfo method, of class UserDAO.
     */
    public void testUpdateContactInfo() throws Exception {
        System.out.println("updateContactInfo");
        String user = "pladmin";
        String expResult = "pladipus42@ugent.be";
        instance.updateContactInfo(user, expResult);
        assertEquals(expResult, instance.getUserContactAddress(user));
    }

}
