package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JDuration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.Nullable;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Duration literal.
 */
public class DurationLiteral extends Literal {
    private final JDuration duration;

    /**
     * Constructor.
     * @param location where
     * @param duration what
     */
    public DurationLiteral(final Location location, final String duration, final BuiltinTypes builtins) {
        super(location);
        this.duration = parse(duration, builtins);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) {
        return duration;
    }

    /**
     * Create a duration from a string.
     * @param text a string
     * @return a duraction, of null when not valid
     */
    public static @Nullable JDuration parse(final String text, final BuiltinTypes builtins) {
        for (final JDuration.JDurationKind value : JDuration.JDurationKind.values()) {
            if (text.endsWith(value.tag)) {
                return builtins.duration(Long.parseLong(text.substring(0, text.length() - value.tag.length())), value);
            }
        }
        return null;
    }


    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("value", duration.janitorToString())
                .endObject();
    }
}
