package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

/**
 * Integer literal.
 */
public class IntegerLiteral extends Literal {
    private final JInt variableInteger;

    /**
     * Constructor.
     * @param location where
     * @param value what
     */
    public IntegerLiteral(final Location location, final JInt value) {
        super(location);
        this.variableInteger = value;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) {
        return variableInteger;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.value(variableInteger.janitorGetHostValue());
    }
}
