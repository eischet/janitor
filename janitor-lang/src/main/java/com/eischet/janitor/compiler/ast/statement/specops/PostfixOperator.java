package com.eischet.janitor.compiler.ast.statement.specops;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import org.jetbrains.annotations.NotNull;

/**
 * Postfix operator.
 * These pick the current value as their expression return value.
 */
public abstract class PostfixOperator extends AnyFixOperator {

    /**
     * Constructor.
     * @param location where
     * @param expr expression
     */
    public PostfixOperator(final Location location, final Expression expr) {
        super(location, expr);
    }

    @Override
    protected @NotNull JanitorObject pick(final JanitorObject currentValue, final JanitorObject newValue) {
        return currentValue.janitorUnpack();
    }
}
