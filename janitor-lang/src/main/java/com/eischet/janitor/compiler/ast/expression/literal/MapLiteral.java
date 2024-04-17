package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JMap;
import com.eischet.janitor.compiler.ast.expression.Expression;

import java.util.List;

public class MapLiteral extends Literal {

    private final List<Preset> presets;

    public static class Preset {
        final Expression key;
        final Expression value;

        public Preset(final Expression key, final Expression value) {
            this.key = key;
            this.value = value;
        }
    }

    public MapLiteral(final Location location, final List<Preset> presets) {
        super(location);
        this.presets = presets;
    }

    @Override
    public JMap evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        final JMap map = new JMap();
        for (final Preset preset : presets) {
            map.put(preset.key.evaluate(runningScript).janitorUnpack(), preset.value.evaluate(runningScript).janitorUnpack());
        }
        return map;
    }
}
