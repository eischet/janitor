package com.eischet.janitor.cleanup.experimental.meta;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JanitorAdapter<T> {

    @NotNull ImmutableCollection<JanitorProperty<T>> getAllProperties();
    @Nullable JanitorProperty<T> getProperty(final String propertyName);

    default boolean hasProperty(final String propertyName) {
        return getProperty(propertyName) != null;
    }

}
