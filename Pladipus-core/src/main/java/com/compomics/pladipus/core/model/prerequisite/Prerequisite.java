/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.core.model.prerequisite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth Verheggen
 */
public class Prerequisite {

    /**
     * A list of prerequisite parameters
     */
    private final ArrayList<PrerequisiteParameter> prerequisiteList = new ArrayList<>();
    /**
     * The Logging instance
     */
    private static final Logger LOGGER = Logger.getLogger(Prerequisite.class);

    public Prerequisite() {

    }

    /**
     *
     * @returns the prerequisiteparameters for this prerequisite
     */
    public ArrayList<PrerequisiteParameter> getPrerequisiteList() {
        return prerequisiteList;
    }

    /**
     * adds a parameter to the list
     *
     * @param parameter the prerequisiteparameter to add
     */
    public void addPrerequisite(PrerequisiteParameter parameter) {
        prerequisiteList.add(parameter);
    }

    /**
     * removes a parameter to the list
     *
     * @param parameter the prerequisiteparameter to add
     */
    public void removePrerequisite(PrerequisiteParameter parameter) {
        prerequisiteList.remove(parameter);
    }

    /**
     * adds a parameter to the list
     *
     * @param parameter the prerequisiteparameter to add
     * @param value the value for said parameter
     */
    public void addPrerequisite(PrerequisiteParameter parameter, String value) throws UnsupportedOperationException {
        prerequisiteList.add(parameter.getCustomOptionValue(parameter, value));
    }

    /**
     *
     * @return boolean determining if the criteria were matched
     * @throws IOException
     */
    public boolean checkPreRequisites() throws IOException {
        for (PrerequisiteParameter aParameter : prerequisiteList) {
            if (!checkPrerequisite(aParameter)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPrerequisite(PrerequisiteParameter parameter) throws IOException {
        boolean complied = false;
        //CORES
        if (parameter.equals(PrerequisiteParameter.CORES)) {
            int requestedCores = Integer.valueOf(parameter.getOptionValue());
            if (requestedCores <= 0) {
                LOGGER.info("0 cores requested, any number of cores allowed for this task");
                complied = true;
            } else {
                int actualCores = Runtime.getRuntime().availableProcessors();
                if (requestedCores <= actualCores) {
                    complied = true;
                } else {
                    LOGGER.error("This machine does not have enough cores :" + requestedCores + " requested," + actualCores + " available");
                    complied = false;
                }
            }
        } //MEMORY
        else if (parameter.equals(PrerequisiteParameter.MEMORY)) {
            int memoryGB = Integer.valueOf(parameter.getOptionValue());
            if (memoryGB <= 0) {
                LOGGER.info("0 RAM requested, any number of cores allowed for this task");
                complied = true;
            } else {
                long actualRAM = Runtime.getRuntime().maxMemory() / 1024 / 1024 / 1024;
                if (memoryGB <= actualRAM) {
                    complied = true;
                } else {
                    LOGGER.error("This machine does not have enough ram :" + memoryGB + "GB requested," + actualRAM + "GB available");
                    complied = false;
                }
            }
        } //DISKSPACE
        else if (parameter.equals(PrerequisiteParameter.DISKSPACE)) {
            int gigabytes = Integer.valueOf(parameter.getOptionValue());
            if (gigabytes <= 0) {
                LOGGER.info("0 DiskSpace requested, any number of cores allowed for this task");
                complied = true;
            } else {
                File temp = new File("test.txt");
                temp.getFreeSpace();
                if (!temp.exists()) {
                    temp.createNewFile();
                }
                temp.deleteOnExit();
                long actualSpace = temp.getFreeSpace() / 1024 / 1024 / 1024;
                if (gigabytes <= actualSpace) {
                    complied = true;
                } else {
                    LOGGER.error("This machine does not have enough space :" + gigabytes + "GB requested," + actualSpace + "GB available");
                    complied = false;
                }
            }
        } //ARCH 64
        else if (parameter.equals(PrerequisiteParameter.ARCH_64)) {
            if (System.getProperty(parameter.getSystemParameterName()).toLowerCase().contains("64")) {
                complied = true;
            } else {
                LOGGER.error("The operating architecture is not suited for this job.");
                complied = false;
            }
        } //ARCH 32
        else if (parameter.equals(PrerequisiteParameter.ARCH_32)) {
            if (System.getProperty(parameter.getSystemParameterName()).toLowerCase().contains("32")) {
                complied = true;
            } else {
                LOGGER.error("The operating architecture is not suited for this job.");
                complied = false;
            }
        } //OS WINDOWS
        else if (parameter.equals(PrerequisiteParameter.OS_WINDOWS)) {
            if (System.getProperty(parameter.getSystemParameterName()).toLowerCase().contains("win")) {
                complied = true;
            } else {
                LOGGER.error("The operating system is not suited for this job.");
            }
        } //OS LINUX
        else if (parameter.equals(PrerequisiteParameter.OS_LINUX)) {
            String[] linuxNames = new String[]{"linux", "ubuntu", "red"};
            for (String aLinuxName : linuxNames) {
                if (System.getProperty(parameter.getSystemParameterName()).toLowerCase().contains(aLinuxName)) {
                    complied = true;
                }
            }
            LOGGER.error("The operating system is not suited for this job.");
            complied = false;
        } else {
            try {
                complied = System.getProperty(parameter.getSystemParameterName()).equalsIgnoreCase(parameter.getOptionValue());
            } catch (Exception e) {
                complied = false;
            }
        }
        return complied;
    }
}
