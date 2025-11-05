/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;

import org.intellij.lang.annotations.Language;

public class GenericStatement {
    private final String sql;

    public GenericStatement(@Language("SQL") final String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String toString() {
        return String.format("%s{sql=%s}", getClass().getSimpleName(), sql);
    }
}
