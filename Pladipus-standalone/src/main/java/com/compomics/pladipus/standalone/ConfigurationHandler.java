package com.compomics.pladipus.standalone;

import com.compomics.pladipus.core.control.runtime.steploader.StepLoadingException;
import com.compomics.pladipus.core.control.runtime.steploader.impl.SpringProcessingStepLoader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 *
 * @author compomics
 */
public class ConfigurationHandler {

    private static SpringProcessingStepLoader loader;

    private static final Logger LOGGER = Logger.getLogger(ConfigurationHandler.class);
    
    public static File getCurrentDirectory() throws URISyntaxException {
        final Class<?> referenceClass = ConfigurationHandler.class;
        final URL url = referenceClass.getProtectionDomain().getCodeSource().getLocation();
        final File jarPath = new File(url.toURI()).getParentFile();
        System.out.println(jarPath); // this is the path you want 
        return jarPath;
    }

    public static void checkConfiguration() throws URISyntaxException, IOException, StepLoadingException {
        File dest = new File(getCurrentDirectory(), "processing-beans.xml");
        if (!dest.exists()) {
            try ( //check appcontext location
                    InputStream is = ConfigurationHandler.class.getResourceAsStream("/processing-beans.xml");
                    FileWriter out = new FileWriter(dest);
                    BufferedReader in = new BufferedReader(new InputStreamReader(is));) {
                String line;
                while ((line = in.readLine()) != null) {
                    out.append(line).append(System.lineSeparator()).flush();
                }
            }
        }
        File jarRepo = new File(getCurrentDirectory(), "external");
        if (!jarRepo.exists()) {
            jarRepo.mkdirs();
        }
        if (jarRepo.listFiles().length == 0) {
            LOGGER.error("There are no jar files in the jar repository !");
        }
        loader = new SpringProcessingStepLoader(dest, jarRepo);
    }

    public static SpringProcessingStepLoader getLoader() {
        return loader;
    }

}
