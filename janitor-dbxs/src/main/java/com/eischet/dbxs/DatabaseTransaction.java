// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;

/** A unit of work that is executed within a transaction on a database connection. */
@FunctionalInterface
public interface DatabaseTransaction {
    /**
     * Runs the code on the connection of the transaction.
     * @param conn the database connection
     * @throws DatabaseError on database errors
     */
    void apply(DatabaseConnection conn) throws DatabaseError;
}
