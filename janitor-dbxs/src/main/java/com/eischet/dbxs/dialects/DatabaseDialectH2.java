package com.eischet.dbxs.dialects;

import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Set;

public class DatabaseDialectH2 extends DatabaseDialectCommon {

    /** {@code LIMIT ? OFFSET ?} is supported by every version we run against. */
    @Override
    public boolean canLimitAndOffset(final DatabaseVersion databaseVersion) {
        return true;
    }

    @Override
    public @NotNull SelectStatement addLimitAndOffset(final @NotNull SelectStatement selectStatement) {
        return appendLimitThenOffset(selectStatement);
    }

    @Override
    public @NotNull SimplePreparedStatement addLimitAndOffset(final @NotNull SimplePreparedStatement statement, final int limit, final int offset) throws SQLException {
        return bindLimitThenOffset(statement, limit, offset);
    }

    private static final Set<String> KEYWORDS = Set.of("key");

    @Override
    public @NotNull String quoteColumn(final @NotNull String columnName) {
        final String lcc = columnName == null ? null : columnName.toLowerCase();
        if (lcc != null && KEYWORDS.contains(lcc)) {
            return "\"" + lcc + "\"";
        } else {
            return columnName;
        }
    }

    @Override
    public SelectStatement getNextValueQuery(final @Nullable String schema, final @NotNull String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select next value for " + seq);
        } else {
            return new SelectStatement("select next value for " + schema + "." + seq);
        }
    }


}
