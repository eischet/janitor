package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JString;
import com.eischet.janitor.api.types.JanitorObject;

public class StringLiteral extends Literal {

    private final JString constantString;

    public StringLiteral(final Location location, final JString constantString) {
        super(location);
        this.constantString = constantString;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) {
        return constantString;
    }
}
