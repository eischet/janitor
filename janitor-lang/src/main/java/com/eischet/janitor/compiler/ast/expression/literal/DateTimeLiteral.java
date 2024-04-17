package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JDateTime;
import com.eischet.janitor.api.types.JanitorObject;

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
