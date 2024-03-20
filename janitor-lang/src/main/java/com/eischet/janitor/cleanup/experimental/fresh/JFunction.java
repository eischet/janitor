package com.eischet.janitor.cleanup.experimental.fresh;

import org.eclipse.collections.api.factory.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JFunction implements JClass {

    public static final JClass CLASS = new JFunction();

    public JFunction() {
    }

    @Override
    public @NotNull Iterable<String> getPropertyNames() {
        return Lists.immutable.empty();
    }

    @Override
    public @Nullable JClass getPropertyClass(final String name) {
        return null;
    }

    @Override
    public @Nullable JObject getProperty(final String name, final JObject instance) {
        return null;
    }

    @Override
    public @NotNull JAssignmentResult assignProperty(final String name, final JObject instance) {
        return JAssignmentResult.INVALID;
    }

    @Override
    public @NotNull JAssignmentResult assignable(final String name, final JClass jClass) {
        return JAssignmentResult.INVALID;
    }

    @Override
    public JClass jGetClass() {
        return CLASS;
    }

}
