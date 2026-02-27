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

    public @NotNull String getSqlFragment() {
        return sqlFragment;
    }

    public @Nullable ExpressionPrepperBuilder buildPrepper() {
        return prepper;
    }
}
