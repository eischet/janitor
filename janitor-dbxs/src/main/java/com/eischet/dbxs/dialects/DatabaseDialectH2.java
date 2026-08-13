package com.eischet.dbxs.dialects;

import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DatabaseDialectH2 extends DatabaseDialectCommon {

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
