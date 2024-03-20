package com.eischet.janitor.cleanup.compiler.ast.expression.literal;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JInt;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

public class IntegerLiteral extends Literal {
    private final JInt variableInteger;

    public IntegerLiteral(final Location location, final long value) {
        super(location);
        this.variableInteger = JInt.of(value);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) {
        return variableInteger;
    }

}
