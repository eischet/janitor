package com.eischet.janitor.orm.cache;

import com.eischet.janitor.orm.entity.OrmEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A small, pluggable read-through cache for single entities, addressable by numeric ID and by the
 * entity's own lookup key (see {@link OrmEntity#getId()}/{@link OrmEntity#getKey()}). Meant to be wired
 * into a {@code GenericDao} via its {@code enableCache(EntityCache)} method, which also takes care of
 * keeping the cache in sync on insert/update/delete via the DAO's existing change-listener mechanism.
 * <p>
 * This provides a generic version of the common "two parallel caches" pattern (one cache by ID, one by key,
 * TTL-only eviction, hand-rolled per DAO subclass) directly in janitor-orm,
 * so any {@code Dao} can opt into caching without reimplementing it. {@link #noop()} is the
 * default — attaching no cache at all — so nothing changes for existing DAOs unless they explicitly opt
 * in.
 * <p>
 * Implementations must keep the by-id and by-key views consistent with each other purely from what's
 * passed to {@link #put}/{@link #invalidateById}/{@link #invalidateByKey} — never by re-reading
 * {@link OrmEntity#getKey()} off a live entity at invalidation time, since a caller may have already
 * mutated that entity's key in place before invalidating it (a naive implementation would remove the
 * cache entry under the entity's *current* key instead of whatever key it was originally cached under,
 * leaving a stale entry behind).
 *
 * @param <T> the cached entity type
 */
public interface EntityCache<T extends OrmEntity> {

    /**
     * Looks up a cached entity by its numeric ID.
     *
     * @param id the ID to look up
     * @return the cached entity, or {@code null} on a cache miss (including an expired or absent entry)
     */
    @Nullable T findById(long id);

    /**
     * Looks up a cached entity by its lookup key.
     *
     * @param key the key to look up; {@code null} always misses
     * @return the cached entity, or {@code null} on a cache miss
     */
    @Nullable T findByKey(@Nullable String key);

    /**
     * Stores (or refreshes) an entity in the cache, indexed by both its ID and its current key.
     * If this ID was already cached under a different key, that stale by-key entry is removed too.
     *
     * @param entity the entity to cache; {@code null} is silently ignored
     */
    void put(@Nullable T entity);

    /**
     * Removes whatever entity is cached under the given ID, and cleans up its by-key entry too (using
     * the key it was actually cached under, not a key read off some live entity object).
     *
     * @param id the ID to invalidate
     */
    void invalidateById(long id);

    /**
     * Removes whatever entity is cached under the given key, and cleans up its by-id entry too.
     *
     * @param key the key to invalidate; {@code null} is a no-op
     */
    void invalidateByKey(@Nullable String key);

    /**
     * Removes every entry from this cache.
     */
    void clear();

    /**
     * Returns a cache that never stores anything and always misses. This is the default for every DAO
     * that doesn't opt into caching, so attaching no cache changes no behavior.
     */
    static <T extends OrmEntity> EntityCache<T> noop() {
        return NoopEntityCache.instance();
    }

}
