/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public class GenericStatement {
    private final @NotNull String sql;

    public GenericStatement(@Language("SQL") final @NotNull String sql) {
        this.sql = sql;
    }

    public @NotNull String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return String.format("%s{sql=%s}", getClass().getSimpleName(), sql);
    }
}
