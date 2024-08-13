package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Today literal: @today.
 */
public class TodayLiteral extends Literal {

    /**
     * Constructor.
     * @param location where
     */
    public TodayLiteral(final Location location) {
        super(location);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        return process.getBuiltins().today();
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject().optional("type", simpleClassNameOf(this)).endObject();
    }
}
