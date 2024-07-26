package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JMap;
import com.eischet.janitor.compiler.ast.expression.Expression;

import java.util.List;

/**
 * Map literal.
 */
public class MapLiteral extends Literal {

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
     * @param location where
     * @param presets what
     */
    public MapLiteral(final Location location, final List<Preset> presets) {
        super(location);
        this.presets = presets;
    }

    @Override
    public JMap evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        final JMap map = runningScript.getEnvironment().getBuiltins().map();
        for (final Preset preset : presets) {
            map.put(preset.key.evaluate(runningScript).janitorUnpack(), preset.value.evaluate(runningScript).janitorUnpack());
        }
        return map;
    }
}
