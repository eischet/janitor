// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.exceptions.DatabaseError;
import org.jetbrains.annotations.NotNull;

/** Manages the access to one database: it knows the database dialect and the default schema, and runs transactions. */
public interface DataManager {
    /**
     * @return the default schema of the database, or null if there is none
     */
    String getDefaultSchema();

    @NotNull DatabaseDialect getDialect();

    /**
     * @return a description of the use of this data manager, for diagnostics
     */
    String getStatistics();

    /**
     * Runs a function in a transaction, and returns its result. The transaction is committed if the function succeeds, and rolled back if it fails.
     * @param callable the function to run
     * @param <T> the type of the result
     * @return the result of the function
     * @throws DatabaseError if the function or the transaction fails
     */
    <T> T callTransaction(DatabaseFunction<DatabaseConnection, T> callable) throws DatabaseError;

    /**
     * Runs some code in a transaction. The transaction is committed if the code succeeds, and rolled back if it fails.
     * @param transaction the code to run
     * @throws DatabaseError if the code or the transaction fails
     */
    void executeTransaction(DatabaseTransaction transaction) throws DatabaseError;

    /**
     * Runs some code in a transaction, without waiting for the result; errors are logged.
     * @param transaction the code to run
     */
    void scheduleTransaction(DatabaseTransaction transaction);

    /**
     * @return the name of this data manager
     */
    String getName();

    /**
     * @return the schema that statements are run against
     */
    String getSchema();
}
