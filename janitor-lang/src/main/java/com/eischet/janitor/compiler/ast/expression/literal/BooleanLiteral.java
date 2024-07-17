package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JBool;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Boolean literal: true, false.
 */
public class BooleanLiteral extends Literal {
    private final JBool value;

    /**
     * Constructor.
     * @param location where
     * @param value what
     */
    public BooleanLiteral(final Location location, final JBool value) {
        super(location);
        this.value = value;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return value;
    }
}
