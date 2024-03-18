package com.eischet.janitor.compiler.ast.statement.specops;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;

public abstract class PrefixOperator extends AnyFixOperator {

    public PrefixOperator(final Location location, final Expression expr) {
        super(location, expr);
    }

    @Override
    protected JanitorObject pick(final JanitorObject currentValue, final JanitorObject newValue) {
        return newValue.janitorUnpack();
    }
}