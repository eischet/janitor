package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JDate;
import com.eischet.janitor.api.types.JanitorObject;

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
