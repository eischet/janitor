package com.eischet.dbxs.dialects;


import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public abstract class DatabaseDialectCommon implements DatabaseDialect {

    @Override
    public boolean limitAndOffsetRequiresOrderBy() {
        return false; // usually, no, I think
    }

    @Override
    public boolean canLimitAndOffset(final DatabaseVersion databaseVersion) {
        return false; // LATER: für weitere Datenbank-Typen implementieren
    }

    @Override
    public @NotNull SelectStatement addLimitAndOffset(final @NotNull SelectStatement selectStatement) {
        return selectStatement;
    }

    @Override
    public @NotNull SimplePreparedStatement addLimitAndOffset(final @NotNull SimplePreparedStatement statement, final int limit, final int offset) throws SQLException {
        return statement;
    }

    @Override
    public @Nullable SelectStatement getNextValueQuery(final @Nullable String schema, final @NotNull String seq) {
        return null;
    }

    @Override
    public @Nullable SelectStatement getCurrentValueQuery(final @Nullable String schema, final @NotNull String seq) {
        return null;
    }
}
