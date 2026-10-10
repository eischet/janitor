// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

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

/** The dialect for Microsoft SQL Server. */
public class DatabaseDialectMicrosoft extends DatabaseDialectCommon {

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("key", "forbidden"));

    @Override
    public boolean limitAndOffsetRequiresOrderBy() {
        return true; // required for ms sql
    }

    @Override
    public boolean canLimitAndOffset(final DatabaseVersion databaseVersion) {
        return databaseVersion.getMajorVersion() >= 11;
    }


    @Override
    public @NotNull SelectStatement addLimitAndOffset(final @NotNull SelectStatement selectStatement) {
        return new SelectStatement(selectStatement.getSql() + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    }

    // LATER: we will probably need two variants: one that applies limit/offset FIRST, and this one, which applies it at the end.

    @Override
    public @NotNull SimplePreparedStatement addLimitAndOffset(final @NotNull SimplePreparedStatement statement, final int limit, final int offset) throws SQLException {
        return statement.addInt(offset).addInt(limit);
    }


    @Override
    public boolean isAdditionalLikeWildcard(final char c) {
        return c == '[';
    }

    /**
     * Unchanged on purpose: with the usual case-insensitive collations, comparisons already ignore case,
     * and wrapping the column in lower() would defeat index usage.
     */
    @Override
    public @NotNull String foldCase(final @NotNull String expression) {
        return expression;
    }

    @Override
    public @NotNull String quoteColumn(final @NotNull String columnName) {
        if (columnName != null && KEYWORDS.contains(columnName.toLowerCase())) {
            return "[" + columnName + "]";
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
        return null;
    }


}
