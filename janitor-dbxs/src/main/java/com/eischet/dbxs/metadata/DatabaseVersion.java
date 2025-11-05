/*
 * Copyright © 2021 Eischet Software e.K.
 */

package com.eischet.dbxs.metadata;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.exceptions.DatabaseError;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class DatabaseVersion {

    private static final Logger log = LoggerFactory.getLogger(DatabaseVersion.class);

    private int majorVersion;
    private int minorVersion;
    private String productName;
    private String productVersion;

    private static String reflow(final String s) {
        if (s == null || s.isEmpty() || !s.contains("\n")) {
            return s;
        } else {
            return s.replace("\n", " ").replace("  ", " ");
        }
    }

    @NotNull
    public static DatabaseVersion getDatabaseVersion(final DataManager dataManager) {
        final DatabaseVersion databaseVersion = new DatabaseVersion();
        try {
            dataManager.executeTransaction(conn -> {
                try {
                    final DatabaseMetaData databaseMetaData = conn.getJdbcConnection().getMetaData();
                    try {
                        databaseVersion.setProductName(reflow(databaseMetaData.getDatabaseProductName()));
                    } catch (SQLException ignored) {
                    }
                    try {
                        databaseVersion.setProductVersion(reflow(databaseMetaData.getDatabaseProductVersion()));
                    } catch (SQLException ignored) {
                    }
                    try {
                        databaseVersion.setMajorVersion(databaseMetaData.getDatabaseMajorVersion());
                    } catch (SQLException ignored) {
                    }
                    try {
                        databaseVersion.setMinorVersion(databaseMetaData.getDatabaseMinorVersion());
                    } catch (SQLException ignored) {
                    }
                } catch (SQLException e) {
                    log.warn("error fetching database meta data", e);
                }
            });
        } catch (DatabaseError ignored) {
        }
        return databaseVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(final int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(final int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(final String productVersion) {
        this.productVersion = productVersion;
    }

    @Override
    public String toString() {
        return "DatabaseVersion{" +
            "name='" + productName + '\'' +
            ", version='" + productVersion + '\'' +
            ", major=" + majorVersion +
            ", minor=" + minorVersion +
            '}';
    }
}
