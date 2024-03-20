package com.eischet.janitor.cleanup.compiler.ast.statement.specops;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.runtime.JanitorSemantics;

public class PrefixDecrement extends PrefixOperator {

    public PrefixDecrement(final Location location, final Expression expr) {
        super(location, expr);
    }

    @Override
    protected JanitorObject operate(final JanitorScriptProcess runningScript, final JanitorObject currentValue) throws JanitorRuntimeException {
        return JanitorSemantics.decrement(runningScript, currentValue);
    }
}
