package com.compomics.pladipus.core.database;

import com.compomics.pladipus.core.control.distribution.service.database.AutoCloseableDBConnection;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import junit.framework.TestCase;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.h2.tools.Server;

/**
 *
 * @author Kenneth Verheggen
 */
public abstract class MockDatabaseTest extends TestCase {

    private Server httpServer;
    private java.sql.Connection conn;

    public MockDatabaseTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            String jdbcAdress = "jdbc:h2:~/pladipus";
            String jdbcPort = "9941";
            //set up the H2 server
            httpServer = Server.createTcpServer(
                    new String[]{"-tcpPort", jdbcPort, "-tcpAllowOthers"}).start();
            //init the script
            Class.forName("org.h2.Driver");
            conn = DriverManager.
                    getConnection(jdbcAdress + ";"
                            + "MODE=MYSQL;"
                            + "DB_CLOSE_DELAY=60000;"
                            + "DATABASE_TO_UPPER=FALSE;"
                            + "IGNORECASE=TRUE;", "root", "");
            conn.setAutoCommit(true);
            ScriptRunner runner = new ScriptRunner(conn);
            runner.setAutoCommit(true);
            //runner.setLogWriter(null);
            //runner.setErrorLogWriter(null);
            runner.setStopOnError(false);

            runner.runScript(new FileReader(new File(MockDatabaseTest.class.getResource("/PLADIPUS_INIT_SCRIPT.sql").getFile())));
            Class.forName("org.h2.Driver");
            AutoCloseableDBConnection.setConnectionString(jdbcAdress);

            //mark the files for deletion
            //delete the H2 file in the user home
            File h2DB = new File(System.getProperty("user.home") + "/pladipus.h2.db");
            File h2DBTrace = new File(System.getProperty("user.home") + "/pladipus.trace.db");
            h2DB.deleteOnExit();
            h2DBTrace.deleteOnExit();

            System.out.println("EXECUTED");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        conn.close();
        httpServer.stop();

    }

}
