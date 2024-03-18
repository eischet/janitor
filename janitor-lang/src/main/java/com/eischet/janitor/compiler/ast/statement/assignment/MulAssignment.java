package com.eischet.janitor.compiler.ast.statement.assignment;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.runtime.JanitorSemantics;

public class MulAssignment extends Assignment {
    public MulAssignment(final Location location, final Expression left, final Expression right) {
        super(location, left, right);
    }

    @Override
    protected JanitorObject produce(final Expression left, final Expression right, final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return JanitorSemantics.multiply(runningScript, left.evaluate(runningScript), right.evaluate(runningScript).janitorUnpack());
    }
}