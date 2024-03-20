package com.eischet.janitor.cleanup.compiler.ast.expression.literal;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JDateTime;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

public class DateTimeLiteral extends Literal {
    private final JDateTime constantDateTime;

    public DateTimeLiteral(final Location location, final JDateTime date) {
        super(location);
        this.constantDateTime = date;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) {
        return constantDateTime;
    }

}
