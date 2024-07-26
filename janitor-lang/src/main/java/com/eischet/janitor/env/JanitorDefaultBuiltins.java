package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorBuiltins;
import com.eischet.janitor.api.types.JString;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JanitorDefaultBuiltins implements JanitorBuiltins {

    private final JString emptyString;

    public JanitorDefaultBuiltins() {
        emptyString = JString.of("");
    }


    @Override
    public JString emptyString() {
        return emptyString;
    }

    @Override
    public JString string(final @NotNull String value) {
        return JString.of(value);
    }

    @Override
    public JanitorObject nullableString(final @Nullable String value) {
        return JString.ofNullable(value);
    }
}
