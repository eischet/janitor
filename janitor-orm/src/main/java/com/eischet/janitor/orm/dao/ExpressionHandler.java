// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Expression handler for custom SQL fragments and statement preppers.
 */
public class ExpressionHandler {
    private final @NotNull String sqlFragment;
    private final @Nullable ExpressionPrepperBuilder prepper;

    public ExpressionHandler(@NotNull final String sqlFragment, @Nullable final ExpressionPrepperBuilder prepper) {
        this.sqlFragment = sqlFragment;
        this.prepper = prepper;
    }

    /**
     * @return the SQL text for the expression
     */
    public @NotNull String getSqlFragment() {
        return sqlFragment;
    }

    /**
     * @return the builder for the prepper that binds the values of the expression, or null if the SQL needs no parameters
     */
    public @Nullable ExpressionPrepperBuilder buildPrepper() {
        return prepper;
    }
}
