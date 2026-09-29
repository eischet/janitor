package com.eischet.janitor.api.metadata;

import com.eischet.janitor.api.types.interop.NullableGetter;
import com.eischet.janitor.api.types.interop.NullableSetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A handle for a single property registered on a dispatch table via one of its {@code addXxxProperty}
 * methods: its name, its metadata (read back, not just written -- see {@link #getMetaData}), and the
 * getter/setter it was registered with. Intended to be captured once, as a {@code public static final}
 * field right next to the property registration, and passed around afterwards instead of the bare
 * property-name string that every consumer would otherwise have to retype (and could typo).
 * <p>
 * This only carries what any property has in general: a name, a getter, an optional setter, and whatever
 * metadata was attached. Anything more specific -- a max length, a column type, and so on -- lives in that
 * metadata and is read back with {@link #getMetaData}; this type deliberately doesn't grow a convenience
 * accessor per metadata key.
 *
 * @param <T> the type of object the property is defined on
 * @param <V> the property's value type
 */
public interface PropertyHandle<T, V> extends MetaDataBuilder<T> {

    /**
     * The name this property was registered under.
     */
    @NotNull String getName();

    /**
     * Reads back metadata attached to this property, e.g. via {@link #setMetaData} at registration time
     * (chained onto the {@code addXxxProperty} call) or afterwards.
     *
     * @param key   the metadata key
     * @param <K>   the type of value stored under that key
     * @return the stored value, or {@code null} if nothing was stored under that key
     */
    <K> @Nullable K getMetaData(@NotNull MetaDataKey<K> key);

    /**
     * The getter this property was registered with.
     */
    @NotNull NullableGetter<T, V> getGetter();

    /**
     * The setter this property was registered with, or {@code null} if the property is read-only.
     */
    @Nullable NullableSetter<T, V> getSetter();

    /**
     * Whether this property has no setter, i.e. {@link #getSetter()} returns {@code null}.
     */
    default boolean isReadOnly() {
        return getSetter() == null;
    }

    @Override
    <K> PropertyHandle<T, V> setMetaData(@NotNull MetaDataKey<K> key, @Nullable K value);

}
