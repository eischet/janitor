package com.eischet.janitor.cleanup.compiler.ast.statement.specops;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;

public abstract class PostfixOperator extends AnyFixOperator {

    public PostfixOperator(final Location location, final Expression expr) {
        super(location, expr);
    }

    @Override
    protected JanitorObject pick(final JanitorObject currentValue, final JanitorObject newValue) {
        return currentValue.janitorUnpack();
    }
}
