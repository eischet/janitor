package com.eischet.janitor.experimental.fresh;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class in the Janitor language runtime.
 */
public interface JClass extends JObject {
    @NotNull Iterable<String> getPropertyNames();
    @Nullable JClass getPropertyClass(final String name);
    @Nullable JObject getProperty(final String name, final JObject instance);
    @NotNull JAssignmentResult assignProperty(final String name, final JObject instance);
    @NotNull JAssignmentResult assignable(final String name, final JClass jClass);
}
