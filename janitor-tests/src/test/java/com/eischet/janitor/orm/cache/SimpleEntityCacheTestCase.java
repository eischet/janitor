// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.cache;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.entity.OrmEntity;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Regression tests for SimpleEntityCache, in particular the key-change invalidation bug that a naive
 * cache implementation suffers from: it removes a cache entry under an entity's
 * *current* key at invalidation time, which left a stale entry behind under the *old* key whenever a
 * caller changed an entity's key in place before invalidating/re-caching it. SimpleEntityCache avoids
 * this by remembering the key each entry was actually cached under, instead of re-reading it live.
 */
public class SimpleEntityCacheTestCase extends JanitorTest {

    static class TestEntity implements OrmEntity {
        private long id;
        private String key;
        private String name;
        private boolean softDeleted;

        @Override
        public long getId() {
            return id;
        }

        @Override
        public void setId(final long id) {
            this.id = id;
        }

        @Override
        public @Nullable String getKey() {
            return key;
        }

        @Override
        public void setKey(final String key) {
            this.key = key;
        }

        @Override
        public @Nullable String getName() {
            return name;
        }

        @Override
        public void setName(final String name) {
            this.name = name;
        }

        @Override
        public boolean isSoftDeleted() {
            return softDeleted;
        }

        @Override
        public void setSoftDeleted(final boolean softDeleted) {
            this.softDeleted = softDeleted;
        }
    }

    private static TestEntity entity(final long id, final String key) {
        final TestEntity entity = new TestEntity();
        entity.setId(id);
        entity.setKey(key);
        return entity;
    }

    @Test
    public void putThenFindByIdHits() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 100);
        final TestEntity e = entity(1, "A");
        cache.put(e);
        assertSame(e, cache.findById(1));
    }

    @Test
    public void putThenFindByKeyHits() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 100);
        final TestEntity e = entity(1, "A");
        cache.put(e);
        assertSame(e, cache.findByKey("A"));
    }

    @Test
    public void missesReturnNull() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 100);
        assertNull(cache.findById(42));
        assertNull(cache.findByKey("nope"));
        assertNull(cache.findByKey(null));
    }

    @Test
    public void invalidateByIdRemovesBothIndexes() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 100);
        cache.put(entity(1, "A"));
        cache.invalidateById(1);
        assertNull(cache.findById(1));
        assertNull(cache.findByKey("A"));
    }

    @Test
    public void invalidateByKeyRemovesBothIndexes() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 100);
        cache.put(entity(1, "A"));
        cache.invalidateByKey("A");
        assertNull(cache.findById(1));
        assertNull(cache.findByKey("A"));
    }

    @Test
    public void changingAnEntitysKeyAndRePuttingItDoesNotLeaveAStaleOldKeyEntry() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 100);
        final TestEntity e = entity(1, "OLD");
        cache.put(e);
        assertSame(e, cache.findByKey("OLD"));

        // caller renames the entity in place, then re-caches it (e.g. after an update) -- this is
        // exactly the scenario a naive invalidate() gets wrong, because it would have looked at
        // e.getKey() (now "NEW") to decide what to evict, instead of what was actually cached ("OLD").
        e.setKey("NEW");
        cache.put(e);

        assertNull(cache.findByKey("OLD"), "the stale old-key entry must be gone once the entity is re-cached under a new key");
        assertSame(e, cache.findByKey("NEW"));
        assertSame(e, cache.findById(1));
    }

    @Test
    public void invalidatingByIdAfterAKeyChangeStillCleansUpTheOriginallyCachedKey() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 100);
        final TestEntity e = entity(1, "OLD");
        cache.put(e);

        // mutate the key in place WITHOUT re-putting -- the cache should still remember "OLD" as the
        // key this id was cached under, and clean that up, not whatever the live entity says now.
        e.setKey("NEW");
        cache.invalidateById(1);

        assertNull(cache.findById(1));
        assertNull(cache.findByKey("OLD"), "invalidateById must evict the key the entry was actually cached under");
        assertNull(cache.findByKey("NEW"), "NEW was never cached under this id, so it must not resolve either");
    }

    @Test
    public void ttlExpiryEvictsEntries() throws InterruptedException {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMillis(1), 100);
        cache.put(entity(1, "A"));
        Thread.sleep(20);
        assertNull(cache.findById(1));
        assertNull(cache.findByKey("A"));
    }

    @Test
    public void sizeBoundEvictsLeastRecentlyUsedEntry() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 2);
        cache.put(entity(1, "A"));
        cache.put(entity(2, "B"));
        cache.put(entity(3, "C")); // exceeds maxSize=2, id 1 was least recently touched -> evicted

        assertNull(cache.findById(1));
        assertNull(cache.findByKey("A"));
        assertEquals(2L, cache.findById(2).getId());
        assertEquals(3L, cache.findById(3).getId());
    }

    @Test
    public void clearRemovesEverything() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 100);
        cache.put(entity(1, "A"));
        cache.put(entity(2, "B"));
        cache.clear();
        assertNull(cache.findById(1));
        assertNull(cache.findById(2));
        assertNull(cache.findByKey("A"));
        assertNull(cache.findByKey("B"));
    }

    @Test
    public void puttingNullIsANoOp() {
        final SimpleEntityCache<TestEntity> cache = new SimpleEntityCache<>(Duration.ofMinutes(5), 100);
        cache.put(null);
        assertNull(cache.findById(0));
    }

}
