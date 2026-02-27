package com.eischet.janitor.orm.dao;

import com.eischet.janitor.orm.filter.FilterExpression;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ExpressionPrepperBuilder {
    @NotNull Prepper getPrepper(final @NotNull FilterExpression expression);
}
