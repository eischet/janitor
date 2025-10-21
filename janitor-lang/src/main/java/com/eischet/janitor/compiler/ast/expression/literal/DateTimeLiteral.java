package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * DateTime literal.
 */
public class DateTimeLiteral extends Literal {
    private final JDateTime constantDateTime;

    /**
     * Constructor.
     * @param location where
     * @param date what
     */
    public DateTimeLiteral(final Location location, final JDateTime date) {
        super(location);
        this.constantDateTime = date;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) {
        return constantDateTime;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("value", constantDateTime.janitorToString())
                .endObject();
    }
}
