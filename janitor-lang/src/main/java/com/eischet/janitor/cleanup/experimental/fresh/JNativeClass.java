package com.eischet.janitor.cleanup.experimental.fresh;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class JNativeClass<T> implements JClass {
    protected abstract @NotNull JPropertyMap getProperties();

    @Override
    public @NotNull Iterable<String> getPropertyNames() {
        return getProperties().getKeys();
    }

    @Override
    public @Nullable JClass getPropertyClass(final String name) {
        final JProp p = getProperties().get(name);
        return p == null ? null : p.getPropertyClass();
    }

    @Override
    public @Nullable JObject getProperty(final String name, final JObject instance) {
        final JProp p = getProperties().get(name);
        return p == null ? null : p.getValue(instance);
    }

    @Override
    public @NotNull JAssignmentResult assignProperty(final String name, final JObject instance) {
        final JProp p = getProperties().get(name);
        if (p == null) {
            return JAssignmentResult.INVALID;
        }
        return p.assignProperty(instance);
    }

    @Override
    public @NotNull JAssignmentResult assignable(final String name, final JClass jClass) {
        final JProp p = getProperties().get(name);
        if (p == null) {
            return JAssignmentResult.INVALID;
        }
        return p.assignable(jClass);
    }

    @Override
    public JClass jGetClass() {
        return this; // was ist die Klasse einer Klasse...?
    }

}
