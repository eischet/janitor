package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Map literal.
 */
public class MapLiteral extends Literal implements JsonExportableObject {

    private final List<Preset> presets;

    /**
     * Presets represent key-value pairs of expressions, as created by the compiler.
     */
    public static class Preset {
        final Expression key;
        final Expression value;

        public Preset(final Expression key, final Expression value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Constructor.
     *
     * @param location where
     * @param presets  what
     */
    public MapLiteral(final Location location, final List<Preset> presets) {
        super(location);
        this.presets = presets;
    }

    @Override
    public @NotNull JMap evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        final JMap map = process.getEnvironment().getBuiltinTypes().map();
        for (final Preset preset : presets) {
            map.put(preset.key.evaluate(process).janitorUnpack(), preset.value.evaluate(process).janitorUnpack());
        }
        return map;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .key("presets").beginArray();
        for (Preset preset : presets) {
            producer.beginObject()
                    .optional("key", preset.key)
                    .optional("value", preset.value)
                    .endObject();
        }
        producer.endArray()
                .endObject();
    }


}
