package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JDuration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;

public class DurationLiteral extends Literal {
    private final JDuration duration;

    public DurationLiteral(final Location location, final JDuration duration) {
        super(location);
        this.duration = duration;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) {
        return duration;
    }

}
