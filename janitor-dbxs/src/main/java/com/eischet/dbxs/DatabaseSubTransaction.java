package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;

/**
 * This is the same as DatabaseTransaction, but provided as a different interface for use cases
 * where we want to make it clear that some code runs in the context of another transaction.
 * For example, this is used in a client app to add some additional SQL updates in certain cases.
 */
@FunctionalInterface
public interface DatabaseSubTransaction {
    /**
     * Runs the code on the connection of the surrounding transaction.
     * @param conn the database connection
     * @throws DatabaseError on database errors
     */
    void apply(DatabaseConnection conn) throws DatabaseError;
}
