package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorBuiltins;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

/**
 * Float literal.

 */
public class FloatLiteral extends Literal {
    private final JFloat variableFloat;

    /**
     * Constructor.
     *
     * @param location where
     * @param value    what
     * @param builtins
     */
    public FloatLiteral(final Location location, final double value, final @NotNull JanitorBuiltins builtins) {
        super(location);
        this.variableFloat = builtins.floatingPoint(value);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return variableFloat;
    }
}
