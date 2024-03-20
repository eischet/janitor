package com.eischet.janitor.cleanup.compiler.ast.expression.literal;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JDate;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

public class DateLiteral extends Literal {
    private final JDate constantDate;

    public DateLiteral(final Location location, final JDate date) {
        super(location);
        this.constantDate = date;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) {
        return constantDate;
    }

}
