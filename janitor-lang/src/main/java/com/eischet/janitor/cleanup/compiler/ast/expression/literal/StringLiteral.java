package com.eischet.janitor.cleanup.compiler.ast.expression.literal;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JString;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

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
