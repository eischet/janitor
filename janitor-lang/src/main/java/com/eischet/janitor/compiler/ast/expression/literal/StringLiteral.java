package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

/**
 * String literal: "foobar".
 */
public class StringLiteral extends Literal {

    private final JString constantString;

    /**
     * Constructor.
     * @param location where
     * @param constantString what
     */
    public StringLiteral(final Location location, final JString constantString) {
        super(location);
        this.constantString = constantString;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) {
        return constantString;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        // producer.beginObject().optional("type", simpleClassNameOf(this)).endObject();
        producer.value(constantString.janitorGetHostValue());
    }
}
