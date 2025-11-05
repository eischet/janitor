/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.dialects;

import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DatabaseDialectMySql extends DatabaseDialectCommon {

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("key", "read_only", "function"));

    @Override
    public String quoteColumn(final String columnName) {
        if (columnName != null && KEYWORDS.contains(columnName.toLowerCase())) {
            return "`" + columnName + "`";
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
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select previous value for " + seq);
        } else {
            return new SelectStatement("select previous value for " + schema + "." + seq);
        }
    }


}
