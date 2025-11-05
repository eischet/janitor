/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.dialects;

import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public SelectStatement addLimitAndOffset(final SelectStatement selectStatement) {
        return new SelectStatement(selectStatement.getSql() + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    }

    // LATER: es wird zwei Varianten geben müssen: eine, die ZUERST limit/offset setzt, und diese hier die es am Ende tut. Glaube ich.

    @Override
    public SimplePreparedStatement addLimitAndOffset(final SimplePreparedStatement statement, final int limit, final int offset) throws SQLException {
        return statement.addInt(offset).addInt(limit);
    }


    @Override
    public String quoteColumn(final String columnName) {
        if (columnName != null && KEYWORDS.contains(columnName.toLowerCase())) {
            return "[" + columnName + "]";
        } else {
            return columnName;
        }
    }

    @Override
    public SelectStatement getNextValueQuery(final String schema, final String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select next value for " + seq);
        } else {
            return new SelectStatement("select next value for " + schema + "." + seq);
        }
    }

    @Override
    public @Nullable SelectStatement getCurrentValueQuery(final String schema, final String seq) {
        return null;
    }


}
