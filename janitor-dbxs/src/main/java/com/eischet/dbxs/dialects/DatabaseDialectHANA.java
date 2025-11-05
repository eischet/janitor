package com.eischet.dbxs.dialects;

import com.eischet.dbxs.statements.SelectStatement;

public class DatabaseDialectHANA extends DatabaseDialectGeneric {
    @Override
    public SelectStatement getNextValueQuery(final String schema, final String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select " + seq + ".nextval");
        } else {
            return new SelectStatement("select " + schema + "." + seq + ".nextval");
        }
    }
}
