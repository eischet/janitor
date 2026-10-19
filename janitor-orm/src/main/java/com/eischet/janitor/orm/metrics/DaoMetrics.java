// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.metrics;

import org.jetbrains.annotations.NotNull;

/**
 * Receives what one DAO's entity cache does: hits, misses, the time it took to load a missed entity, and evictions. The host decides
 * where these go (see {@link OrmMetrics#install}); janitor-orm only reports them. Implementations must be cheap and thread-safe: they are
 * called on every cached lookup.
 */
public interface DaoMetrics {

    /**
     * Reports nothing; the default until a host installs a {@link DaoMetricsProvider}.
     */
    DaoMetrics NOOP = new DaoMetrics() {
        @Override
        public void cacheHit() {
        }

        @Override
        public void cacheMiss() {
        }

        @Override
        public void loaded(final long nanos) {
        }

        @Override
        public void evicted(final @NotNull EvictionReason reason) {
        }
    };

    /**
     * A lookup was answered from the cache.
     */
    void cacheHit();

    /**
     * A lookup was not in the cache (or expired) and had to go to the database.
     */
    void cacheMiss();

    /**
     * After a miss: how long loading from the database took.
     *
     * @param nanos the duration in nanoseconds
     */
    void loaded(long nanos);

    /**
     * An entry left the cache for the given reason.
     */
    void evicted(@NotNull EvictionReason reason);
}
