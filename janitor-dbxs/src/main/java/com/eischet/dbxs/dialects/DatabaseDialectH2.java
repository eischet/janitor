package com.eischet.dbxs.dialects;

import com.eischet.dbxs.statements.SelectStatement;

import java.util.Set;

public class DatabaseDialectH2 extends DatabaseDialectCommon {

    private static final Set<String> KEYWORDS = Set.of("key");

    @Override
    public String quoteColumn(final String columnName) {
        final String lcc = columnName == null ? null : columnName.toLowerCase();
        if (lcc != null && KEYWORDS.contains(lcc)) {
            return "\"" + lcc + "\"";
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


}
