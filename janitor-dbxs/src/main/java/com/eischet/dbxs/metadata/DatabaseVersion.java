// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs.metadata;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.logging.JanitorLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/** Describes the product and version of a database, as reported by its JDBC driver. */
public class DatabaseVersion {

    private static final JanitorLogger log = JanitorLogger.getLogger(DatabaseVersion.class);

    private int majorVersion;
    private int minorVersion;
    private String productName;
    private String productVersion;

    private static @Nullable String reflow(final @Nullable String s) {
        if (s == null || !s.contains("\n")) {
            return s;
        } else {
            return s.replace("\n", " ").replace("  ", " ");
        }
    }

    /**
     * Asks a database for its product name and version.
     * @param dataManager the data manager of the database
     * @return the version information; fields that could not be determined are left empty
     */
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

    /**
     * @return the major version of the database
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Sets the major version of the database.
     * @param majorVersion the major version
     */
    public void setMajorVersion(final int majorVersion) {
        this.majorVersion = majorVersion;
    }

    /**
     * @return the minor version of the database
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * Sets the minor version of the database.
     * @param minorVersion the minor version
     */
    public void setMinorVersion(final int minorVersion) {
        this.minorVersion = minorVersion;
    }

    /**
     * @return the name of the database product
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the name of the database product.
     * @param productName the product name
     */
    public void setProductName(final String productName) {
        this.productName = productName;
    }

    /**
     * @return the version of the database product, as a string
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * Sets the version of the database product, as a string.
     * @param productVersion the product version
     */
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
