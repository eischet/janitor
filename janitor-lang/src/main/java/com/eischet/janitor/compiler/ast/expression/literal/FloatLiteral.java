package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JFloat;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;

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
