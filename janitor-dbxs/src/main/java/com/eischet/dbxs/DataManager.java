/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.exceptions.DatabaseError;
import org.jetbrains.annotations.NotNull;

public interface DataManager {
    String getDefaultSchema();

    @NotNull DatabaseDialect getDialect();

    String getStatistics();

    <T> T callTransaction(DatabaseFunction<DatabaseConnection, T> callable) throws DatabaseError;

    void executeTransaction(DatabaseTransaction transaction) throws DatabaseError;

    void scheduleTransaction(DatabaseTransaction transaction);

    String getName();

    String getSchema();
}
