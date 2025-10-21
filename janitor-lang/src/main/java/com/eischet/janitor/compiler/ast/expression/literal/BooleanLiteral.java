package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * Boolean literal: true, false.
 */
public class BooleanLiteral extends Literal {
    private final JBool value;

    /**
     * Constructor.
     * @param location where
     * @param value what
     */
    public BooleanLiteral(final @NotNull Location location, final @NotNull JBool value) {
        super(location);
        this.value = value;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        return value;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.value(value.janitorGetHostValue());
    }

}
