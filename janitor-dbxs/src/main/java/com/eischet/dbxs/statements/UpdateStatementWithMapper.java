/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;


import com.eischet.dbxs.GenericStatementConfigurator;
import com.eischet.dbxs.StatementConfigurator;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public class UpdateStatementWithMapper<T> extends UpdateStatement {
    private final @NotNull GenericStatementConfigurator<T> mapper;

    public UpdateStatementWithMapper(@Language("SQL") final @NotNull String sql,
                                     @NotNull GenericStatementConfigurator<T> mapper) {
        super(sql);
        this.mapper = mapper;
    }


    public @NotNull StatementConfigurator getMapper(final @NotNull T value) {
        return rs -> mapper.configure(value, rs);
    }
}
