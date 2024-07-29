package com.eischet.janitor.compiler.ast.statement.specops;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Prefix increment operator: ++i.
 */
public class PrefixIncrement extends PrefixOperator {

    /**
     * Constructor.
     *
     * @param location where
     * @param expr     expression
     */
    public PrefixIncrement(final Location location, final Expression expr) {
        super(location, expr);
    }

    @Override
    protected JanitorObject operate(final JanitorScriptProcess runningScript, final JanitorObject currentValue) throws JanitorRuntimeException {
        return JanitorSemantics.increment(runningScript, currentValue);
    }


}
