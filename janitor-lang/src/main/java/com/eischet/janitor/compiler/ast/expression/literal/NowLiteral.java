package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Now literal: @now.
 */
public class NowLiteral extends Literal {

    /**
     * Constructor.
     * @param location where
     */
    public NowLiteral(final Location location) {
        super(location);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return runningScript.getBuiltins().now();
    }
}
