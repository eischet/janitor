package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;

public class EvaluatedArgument {
    final @Nullable String name;
    final JanitorObject value;

    public EvaluatedArgument(@Nullable final String name, final JanitorObject value) {
        this.name = name;
        this.value = value;
    }

    public @Nullable String getName() {
        return name;
    }

    public JanitorObject getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (name == null) {
            if (value == null) {
                return "null";
            } else {
                return value.toString();
            }
        } else {
            return name + "=" + value;
        }
    }

}
