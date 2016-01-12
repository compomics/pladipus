/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.moff.playground;

import com.compomics.pladipus.moff.logic.MoffInstaller;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 * @author compomics
 */
public class MoffInstallerRunner {
   
   public static void main(String[]args) throws IOException, URISyntaxException{
       MoffInstaller.installMoff();
   } 
    
}
