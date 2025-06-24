package com.eischet.janitor.api.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementors of the interface can supply meta-data for themselves or for attributes.
 *
 * The use case behind this is to attach arbitrary information to Janitor wrapper classes that the interpreter or
 */
public interface HasMetaData {

    <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key);

    <K> @Nullable K getMetaData(final @NotNull String attributeName, final @NotNull MetaDataKey<K> key);

}
