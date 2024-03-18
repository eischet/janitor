package com.eischet.janitor.runtime.types;

import com.eischet.janitor.api.types.JConstant;
import org.jetbrains.annotations.NotNull;

public abstract class NativeFunction implements JConstant, JCallable {

    // LATER: NativeFunction ist eigentlich nur eine Variante der anderen JCallable-Typen, die man zusammenlegen sollte!

    private final @NotNull String name;

    public NativeFunction(final @NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String janitorClassName() {
        return "function";
    }

    @Override
    public String janitorToString() {
        return "[native function " + name + "]";
    }

}
