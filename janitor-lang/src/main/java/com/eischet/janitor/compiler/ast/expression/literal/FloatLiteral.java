package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Float literal.

 */
public class FloatLiteral extends Literal {
    private final JFloat variableFloat;

    /**
     * Constructor.
     *
     * @param location where
     * @param value    what
     * @param builtins
     */
    public FloatLiteral(final Location location, final double value, final @NotNull BuiltinTypes builtins) {
        super(location);
        this.variableFloat = builtins.floatingPoint(value);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        return variableFloat;
    }


    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("value", variableFloat.janitorGetHostValue())
                .endObject();
    }
}
