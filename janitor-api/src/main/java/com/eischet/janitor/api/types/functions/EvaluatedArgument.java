package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;

/** An argument of a function call after evaluation, optionally carrying the name it was passed by (keyword arguments). */
public class EvaluatedArgument {
    final @Nullable String name;
    final JanitorObject value;

    public EvaluatedArgument(@Nullable final String name, final JanitorObject value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name of the argument, or null if it was passed by position
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * @return the value of the argument
     */
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
