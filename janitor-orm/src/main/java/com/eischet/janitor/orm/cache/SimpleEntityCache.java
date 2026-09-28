package com.eischet.janitor.orm.cache;

import com.eischet.janitor.orm.entity.OrmEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A small, JDK-only, size- and TTL-bounded {@link EntityCache}. It has no dependencies beyond the JDK,
 * matching janitor-orm's own "easy to embed" design goal — if you need a more scalable concurrent cache
 * (e.g. backed by Caffeine), implement {@link EntityCache} yourself and attach that instead; this
 * implementation trades some concurrency (everything guarded by one lock) for simplicity.
 * <p>
 * The by-id and by-key views are kept consistent from the entity data actually passed to
 * {@link #put}/{@link #invalidateById}/{@link #invalidateByKey} — each cached entry remembers the key it
 * was cached under, so invalidating it never depends on re-reading {@link OrmEntity#getKey()} off a live
 * entity object that the caller might have already mutated in place (see {@link EntityCache}'s class doc
 * for the bug this avoids).
 *
 * @param <T> the cached entity type
 */
public final class SimpleEntityCache<T extends OrmEntity> implements EntityCache<T> {

    private final long ttlNanos;
    private final int maxSize;
    private final Object lock = new Object();
    private final LinkedHashMap<Long, CacheEntry<T>> byId;
    private final Map<String, Long> keyToId = new HashMap<>();

    /**
     * @param ttl     how long an entry stays valid after being cached
     * @param maxSize the maximum number of entries to hold; the least-recently-used entry is evicted
     *                once this is exceeded
     */
    public SimpleEntityCache(final @NotNull Duration ttl, final int maxSize) {
        this.ttlNanos = ttl.toNanos();
        this.maxSize = maxSize;
        this.byId = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<Long, CacheEntry<T>> eldest) {
                if (size() <= SimpleEntityCache.this.maxSize) {
                    return false;
                }
                final String evictedKey = eldest.getValue().key;
                if (evictedKey != null) {
                    keyToId.remove(evictedKey);
                }
                return true;
            }
        };
    }

    @Override
    public @Nullable T findById(final long id) {
        synchronized (lock) {
            final CacheEntry<T> entry = byId.get(id);
            if (entry == null) {
                return null;
            }
            if (entry.isExpired()) {
                removeLocked(id);
                return null;
            }
            return entry.value;
        }
    }

    @Override
    public @Nullable T findByKey(final @Nullable String key) {
        if (key == null) {
            return null;
        }
        synchronized (lock) {
            final Long id = keyToId.get(key);
            return id == null ? null : findById(id);
        }
    }

    @Override
    public void put(final @Nullable T entity) {
        if (entity == null) {
            return;
        }
        final long id = entity.getId();
        final String newKey = entity.getKey();
        synchronized (lock) {
            final CacheEntry<T> previous = byId.get(id);
            if (previous != null && previous.key != null && !previous.key.equals(newKey)) {
                keyToId.remove(previous.key);
            }
            byId.put(id, new CacheEntry<>(entity, newKey, System.nanoTime() + ttlNanos));
            if (newKey != null) {
                keyToId.put(newKey, id);
            }
        }
    }

    @Override
    public void invalidateById(final long id) {
        synchronized (lock) {
            removeLocked(id);
        }
    }

    @Override
    public void invalidateByKey(final @Nullable String key) {
        if (key == null) {
            return;
        }
        synchronized (lock) {
            final Long id = keyToId.remove(key);
            if (id != null) {
                byId.remove(id);
            }
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            byId.clear();
            keyToId.clear();
        }
    }

    private void removeLocked(final long id) {
        final CacheEntry<T> entry = byId.remove(id);
        if (entry != null && entry.key != null) {
            keyToId.remove(entry.key);
        }
    }

    private static final class CacheEntry<T> {
        private final T value;
        private final String key;
        private final long expiresAtNanos;

        private CacheEntry(final T value, final String key, final long expiresAtNanos) {
            this.value = value;
            this.key = key;
            this.expiresAtNanos = expiresAtNanos;
        }

        private boolean isExpired() {
            return System.nanoTime() - expiresAtNanos > 0;
        }
    }

}
