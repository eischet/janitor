package com.eischet.janitor.compiler.ast.statement.specops;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Prefix decrement operator: --i.
 */
public class PrefixDecrement extends PrefixOperator {

    /**
     * Constructor.
     * @param location where
     * @param expr expression
     */
    public PrefixDecrement(final Location location, final Expression expr) {
        super(location, expr);
    }

    @Override
    protected JanitorObject operate(final JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException {
        return JanitorSemantics.decrement(process, currentValue);
    }
}
