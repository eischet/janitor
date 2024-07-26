package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JDuration;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Duration literal.
 */
public class DurationLiteral extends Literal {
    private final JDuration duration;

    /**
     * Constructor.
     * @param location where
     * @param duration what
     */
    public DurationLiteral(final Location location, final JDuration duration) {
        super(location);
        this.duration = duration;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) {
        return duration;
    }

}
