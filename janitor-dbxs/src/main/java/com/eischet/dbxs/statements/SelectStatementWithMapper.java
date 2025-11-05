/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;

import com.eischet.dbxs.results.ResultSetReader;
import org.intellij.lang.annotations.Language;

public class SelectStatementWithMapper<T> extends SelectStatement {

    private final ResultSetReader<T> mapper;

    public SelectStatementWithMapper(@Language("SQL") final String sql, final ResultSetReader<T> mapper) {
        super(sql);
        this.mapper = mapper;
    }

    public ResultSetReader<T> getMapper() {
        return mapper;
    }
}
