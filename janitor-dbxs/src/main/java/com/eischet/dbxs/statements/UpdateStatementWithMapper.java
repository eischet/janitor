/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;


import com.eischet.dbxs.GenericStatementConfigurator;
import com.eischet.dbxs.StatementConfigurator;
import org.jetbrains.annotations.NotNull;

public class UpdateStatementWithMapper<T> extends UpdateStatement {
    private final GenericStatementConfigurator<T> mapper;

    public UpdateStatementWithMapper(final String sql, GenericStatementConfigurator<T> mapper) {
        super(sql);
        this.mapper = mapper;
    }


    public @NotNull StatementConfigurator getMapper(final T value) {
        return rs -> mapper.configure(value, rs);
    }
}
