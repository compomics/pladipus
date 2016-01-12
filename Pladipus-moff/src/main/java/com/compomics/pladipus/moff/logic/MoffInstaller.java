package com.compomics.pladipus.moff.logic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import sun.misc.Launcher;

/**
 *
 * @author compomics
 */
public class MoffInstaller {

    public static void installMoff() throws IOException, URISyntaxException {
        File destFile = new File(System.getProperty("user.home") + "/pladipus/tools/moff");
        URL resource = MoffInstaller.class.getClassLoader().getResource("moFF");
        File resourceFolder = new File(resource.toURI());
        FileUtils.copyDirectory(resourceFolder, destFile);
    }
}
    
    


