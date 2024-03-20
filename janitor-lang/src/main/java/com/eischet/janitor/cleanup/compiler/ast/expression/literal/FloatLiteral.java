package com.eischet.janitor.cleanup.compiler.ast.expression.literal;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JFloat;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

public class FloatLiteral extends Literal {
    private final JFloat variableFloat;

    public FloatLiteral(final Location location, final double value) {
        super(location);
        this.variableFloat = JFloat.of(value);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return variableFloat;
    }
}
