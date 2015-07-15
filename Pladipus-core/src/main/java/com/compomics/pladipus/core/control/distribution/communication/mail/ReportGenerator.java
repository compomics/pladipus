package com.compomics.pladipus.core.control.distribution.communication.mail;

import java.io.IOException;
import java.sql.SQLException;

public abstract interface ReportGenerator {

    /**
     *
     * @param paramInt the parameter to be reported
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public abstract String generateReport(int paramInt)
            throws IOException, SQLException;

    /**
     *
     * @param paramInt the parameter to be reported
     * @return the e-mail subject
     * @throws IOException
     * @throws SQLException
     */
    public abstract String generateSubject(int paramInt)
            throws IOException, SQLException;
}
