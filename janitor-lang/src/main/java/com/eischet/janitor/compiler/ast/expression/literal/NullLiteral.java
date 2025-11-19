package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Null literal: null.
 */
public class NullLiteral extends Literal {

    public static final NullLiteral NULL = new NullLiteral(Location.BUILTIN_LOCATION);

    /**
     * Constructor.
     * @param location where
     */
    private NullLiteral(final @NotNull Location location) {
        super(location);
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) {
        return JNull.NULL;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject().optional("type", simpleClassNameOf(this)).endObject();
    }
}
