/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.dialects;

import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DatabaseDialectMySql extends DatabaseDialectCommon {

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

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("key", "read_only", "function"));

    @Override
    public @NotNull String quoteColumn(final @NotNull String columnName) {
        if (columnName != null && KEYWORDS.contains(columnName.toLowerCase())) {
            return "`" + columnName + "`";
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

    @Override
    public @Nullable SelectStatement getCurrentValueQuery(final @Nullable String schema, final @NotNull String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select previous value for " + seq);
        } else {
            return new SelectStatement("select previous value for " + schema + "." + seq);
        }
    }


}
