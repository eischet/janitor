package com.eischet.janitor.cleanup.experimental.fresh;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class JMethod<JCLASS> implements JObject {

    final String name;

    public JMethod(final String name) {
        this.name = name;
    }

    @Override
    public JClass jGetClass() {
        return JFunction.CLASS;
    }

    @Override
    public final JObject jCall(@Nullable final JObject self, @NotNull final JCallArgs args) {
        return null;
    }

    public abstract JObject jCallMethod(@NotNull final JCLASS self, @NotNull final JCallArgs args);

}
