// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs.statements;


import com.eischet.dbxs.GenericStatementConfigurator;
import com.eischet.dbxs.StatementConfigurator;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

/**
 * An SQL statement that changes data, which comes with the code that sets the statement's parameters from an object.
 * @param <T> the type of the objects
 */
public class UpdateStatementWithMapper<T> extends UpdateStatement {
    private final @NotNull GenericStatementConfigurator<T> mapper;

    public UpdateStatementWithMapper(@Language("SQL") final @NotNull String sql,
                                     @NotNull GenericStatementConfigurator<T> mapper) {
        super(sql);
        this.mapper = mapper;
    }


    /**
     * @param value the object to take the parameters from
     * @return a configurator that sets the parameters of the statement from the object
     */
    public @NotNull StatementConfigurator getMapper(final @NotNull T value) {
        return rs -> mapper.configure(value, rs);
    }
}
