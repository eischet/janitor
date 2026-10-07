package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.api.metadata.PropertyHandle;
import com.eischet.janitor.api.types.interop.NullableGetter;
import com.eischet.janitor.api.types.interop.NullableSetter;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import com.eischet.janitor.versioning.Version;
import com.eischet.janitor.versioning.VersionRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link PropertyHandle} for properties registered through the column methods of an {@link OrmDispatchTable}
 * (and therefore also of {@link EntityDispatchTable} and {@link JoinDispatchTable}), with convenience accessors
 * for the {@link JanitorOrm.MetaData} that those methods attach: column name, column type, maximum length,
 * lazy loading, referenced class, and the schema version range.
 * <p>
 * Like any {@link PropertyHandle}, this is only a view: all values are read from and written to the dispatch
 * table's metadata, so it stays consistent with {@code getMetaData(field, key)} calls on the table itself.
 * </p>
 *
 * @param <T> the type of object the property is defined on
 * @param <V> the property's value type
 */
public interface OrmPropertyHandle<T, V> extends PropertyHandle<T, V> {

    /**
     * Wraps a plain handle, e.g. one obtained from an {@code addXxxProperty} method, to get the ORM accessors.
     */
    static <T, V> @NotNull OrmPropertyHandle<T, V> of(final @NotNull PropertyHandle<T, V> handle) {
        if (handle instanceof OrmPropertyHandle<T, V> orm) {
            return orm;
        }
        return new DefaultOrmPropertyHandle<>(handle);
    }

    /**
     * The database column name, or {@code null} if this property is not backed by a column.
     */
    default @Nullable String getColumnName() {
        return getMetaData(JanitorOrm.MetaData.COLUMN_NAME);
    }

    /**
     * The column type hint, or {@code null} if none was set.
     */
    default @Nullable ColumnTypeHint getColumnType() {
        return getMetaData(JanitorOrm.MetaData.COLUMN_TYPE);
    }

    /**
     * The maximum length of a string column, or {@code null} if there is none.
     */
    default @Nullable Integer getMaxLength() {
        return getMetaData(JanitorOrm.MetaData.MAX_LENGTH);
    }

    /**
     * Whether the column is excluded from the default SELECT and only loaded on demand.
     */
    default boolean isLazyLoaded() {
        return Boolean.TRUE.equals(getMetaData(JanitorOrm.MetaData.LAZY_LOAD));
    }

    /**
     * The simple class name of the referenced entity if this is a foreign key, otherwise {@code null}.
     */
    default @Nullable String getReferencedClassName() {
        return getMetaData(Janitor.MetaData.REF);
    }

    /**
     * The range of schema versions in which this property exists, or {@code null} if it exists in all of them.
     */
    default @Nullable VersionRange getVersionRange() {
        return getMetaData(JanitorOrm.MetaData.VERSION_RANGE);
    }

    /**
     * Whether this property exists in the given schema version. True if no range is set or the version is unknown.
     */
    default boolean isAvailableIn(final @Nullable Version schemaVersion) {
        return isAvailableIn(getVersionRange(), schemaVersion);
    }

    /**
     * The rule behind {@link #isAvailableIn(Version)}, for callers that only have the range at hand:
     * a property is available if there is no range, no known schema version, or the range includes the version.
     */
    static boolean isAvailableIn(final @Nullable VersionRange range, final @Nullable Version schemaVersion) {
        return range == null || schemaVersion == null || range.includes(schemaVersion);
    }

    /**
     * Restricts this property to the given range of schema versions.
     */
    default OrmPropertyHandle<T, V> versionRange(final @NotNull VersionRange range) {
        return setMetaData(JanitorOrm.MetaData.VERSION_RANGE, range);
    }

    /**
     * This property exists since the given schema version (inclusive).
     */
    default OrmPropertyHandle<T, V> since(final @NotNull Version version) {
        return versionRange(VersionRange.startingWith(version));
    }

    /**
     * This property exists until the given schema version (inclusive).
     */
    default OrmPropertyHandle<T, V> until(final @NotNull Version version) {
        return versionRange(VersionRange.endingWith(version));
    }

    @Override
    <K> OrmPropertyHandle<T, V> setMetaData(@NotNull MetaDataKey<K> key, @Nullable K value);

    /**
     * Delegates to the plain handle; metadata lives in the dispatch table, so there is no state to keep in sync.
     */
    final class DefaultOrmPropertyHandle<T, V> implements OrmPropertyHandle<T, V> {
        private final PropertyHandle<T, V> delegate;

        private DefaultOrmPropertyHandle(final PropertyHandle<T, V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public @NotNull String getName() {
            return delegate.getName();
        }

        @Override
        public <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key) {
            return delegate.getMetaData(key);
        }

        @Override
        public @NotNull NullableGetter<T, V> getGetter() {
            return delegate.getGetter();
        }

        @Override
        public @Nullable NullableSetter<T, V> getSetter() {
            return delegate.getSetter();
        }

        @Override
        public <K> OrmPropertyHandle<T, V> setMetaData(final @NotNull MetaDataKey<K> key, final @Nullable K value) {
            delegate.setMetaData(key, value);
            return this;
        }
    }

}
