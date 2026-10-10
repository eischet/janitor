// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs.statements;

import com.eischet.dbxs.results.ResultSetReader;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

/**
 * An SQL query that comes with the code that converts the result rows to objects.
 * @param <T> the type of the objects
 */
public class SelectStatementWithMapper<T> extends SelectStatement {

    private final @NotNull ResultSetReader<T> mapper;

    public SelectStatementWithMapper(@Language("SQL") final String sql, final ResultSetReader<T> mapper) {
        super(sql);
        this.mapper = mapper;
    }

    /**
     * @return the reader that converts a result row to an object
     */
    public @NotNull ResultSetReader<T> getMapper() {
        return mapper;
    }
}
