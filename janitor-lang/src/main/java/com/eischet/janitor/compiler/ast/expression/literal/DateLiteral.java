package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Date literal: @2024-07-21.
 */
public class DateLiteral extends Literal {
    private final JDate constantDate;

    /**
     * Constructor.
     * @param location where
     * @param date what
     */
    public DateLiteral(final Location location, final JDate date) {
        super(location);
        this.constantDate = date;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) {
        return constantDate;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("value", constantDate.janitorToString())
                .endObject();
    }
}
