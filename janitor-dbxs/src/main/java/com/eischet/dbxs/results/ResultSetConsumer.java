/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.results;

import com.eischet.dbxs.exceptions.DatabaseError;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetConsumer {
    void consume(final @NotNull SimpleResultSet rs) throws SQLException, DatabaseError;
}
