package com.eischet.janitor.orm.dao;

import com.eischet.janitor.orm.filter.FilterExpression;
import org.jetbrains.annotations.NotNull;

/** Creates the {@link Prepper} that binds the value of a filter expression to a prepared statement. */
@FunctionalInterface
public interface ExpressionPrepperBuilder {
    @NotNull Prepper getPrepper(final @NotNull FilterExpression expression);
}
