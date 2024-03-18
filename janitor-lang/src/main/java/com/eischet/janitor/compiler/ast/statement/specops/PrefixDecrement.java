package com.eischet.janitor.compiler.ast.statement.specops;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.runtime.JanitorSemantics;

public class PrefixDecrement extends PrefixOperator {

    public PrefixDecrement(final Location location, final Expression expr) {
        super(location, expr);
    }

    @Override
    protected JanitorObject operate(final JanitorScriptProcess runningScript, final JanitorObject currentValue) throws JanitorRuntimeException {
        return JanitorSemantics.decrement(runningScript, currentValue);
    }
}
