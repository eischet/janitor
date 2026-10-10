package com.eischet.janitor.api.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementors of the interface can supply meta-data for themselves or for attributes.
 *
 * The use case behind this is to attach arbitrary information to Janitor wrapper classes that the interpreter or
 */
public interface HasMetaData {
    /**
     * Gets the meta-data of this object.
     * @param key the key
     * @param <K> the type of the value
     * @return the value, or null if there is none
     */
    <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key);
    /**
     * Gets the meta-data of an attribute of this object.
     * @param attributeName the name of the attribute
     * @param key the key
     * @param <K> the type of the value
     * @return the value, or null if there is none
     */
    <K> @Nullable K getMetaData(final @NotNull String attributeName, final @NotNull MetaDataKey<K> key);
}
