// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.cache;

import com.eischet.janitor.orm.entity.OrmEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The default {@link EntityCache}: stores nothing, always misses. Used by every DAO that doesn't
 * explicitly opt into caching via {@code enableCache(...)}.
 */
final class NoopEntityCache<T extends OrmEntity> implements EntityCache<T> {

    private static final NoopEntityCache<?> INSTANCE = new NoopEntityCache<>();

    @SuppressWarnings("unchecked")
    static <T extends OrmEntity> EntityCache<T> instance() {
        return (EntityCache<T>) INSTANCE;
    }

    private NoopEntityCache() {
    }

    @Override
    public @Nullable T findById(final long id) {
        return null;
    }

    @Override
    public @Nullable T findByKey(final @Nullable String key) {
        return null;
    }

    @Override
    public void put(final @Nullable T entity) {
    }

    @Override
    public void invalidateById(final long id) {
    }

    @Override
    public void invalidateByKey(final @Nullable String key) {
    }

    @Override
    public void clear() {
    }

}
