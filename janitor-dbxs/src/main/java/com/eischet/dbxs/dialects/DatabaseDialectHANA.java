package com.eischet.dbxs.dialects;

import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseDialectHANA extends DatabaseDialectGeneric {
    @Override
    public SelectStatement getNextValueQuery(final @Nullable String schema, final @NotNull String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select " + seq + ".nextval");
        } else {
            return new SelectStatement("select " + schema + "." + seq + ".nextval");
        }
    }
}
