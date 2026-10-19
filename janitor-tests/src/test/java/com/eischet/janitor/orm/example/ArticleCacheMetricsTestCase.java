// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.cache.SimpleEntityCache;
import com.eischet.janitor.orm.metrics.CacheState;
import com.eischet.janitor.orm.metrics.DaoMetrics;
import com.eischet.janitor.orm.metrics.EvictionReason;
import com.eischet.janitor.orm.metrics.OrmMetrics;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A DAO with a cache reports hits, misses, load times and evictions to the {@link DaoMetrics} the installed provider gives it; a DAO
 * without a cache reports nothing.
 */
public class ArticleCacheMetricsTestCase extends JanitorTest {

    /**
     * Counts what a DAO reports.
     */
    static final class CountingMetrics implements DaoMetrics {
        final AtomicInteger hits = new AtomicInteger();
        final AtomicInteger misses = new AtomicInteger();
        final List<Long> loads = new ArrayList<>();
        final List<EvictionReason> evictions = new ArrayList<>();

        @Override
        public void cacheHit() {
            hits.incrementAndGet();
        }

        @Override
        public void cacheMiss() {
            misses.incrementAndGet();
        }

        @Override
        public synchronized void loaded(final long nanos) {
            loads.add(nanos);
        }

        @Override
        public synchronized void evicted(final @NotNull EvictionReason reason) {
            evictions.add(reason);
        }
    }

    /**
     * The example DAO with a cache of two entries.
     */
    static final class CachedArticleDao extends ArticleDao {
        CachedArticleDao(final ExampleDaoCollection collection) {
            super(collection);
            enableCache(new SimpleEntityCache<>(Duration.ofMinutes(5), 2));
        }
    }

    private SimpleDataManager manager;
    private ExampleDaoCollection collection;
    private final CountingMetrics counted = new CountingMetrics();

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        collection = new ExampleDaoCollection(manager);
        final ArticleDao plain = collection.getArticleDao();
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_demo_article_id start with 1"));
            conn.update(UpdateStatement.of("""
                    create table demo_article (
                      article_id bigint primary key,
                      article_key varchar(50),
                      short_code varchar(10),
                      article_name varchar(100),
                      description clob,
                      stock integer,
                      active smallint
                    )"""));
            for (int i = 1; i <= 5; i++) {
                final Article article = new Article(collection);
                article.setKey("key-" + i);
                article.setShortCode("c" + i);
                article.setName("Article " + i);
                article.setStock(i);
                article.setActive(true);
                plain.insert(conn, article);
            }
        });
        OrmMetrics.install(dao -> dao instanceof CachedArticleDao ? counted : DaoMetrics.NOOP);
    }

    @AfterEach
    void uninstall() {
        OrmMetrics.install(dao -> DaoMetrics.NOOP);
    }

    @Test
    void hitsMissesLoadsAndEvictionsAreReported() throws DatabaseError {
        final CachedArticleDao dao = new CachedArticleDao(collection);
        manager.executeTransaction(conn -> {
            assertNotNull(dao.findById(conn, 1));      // miss, load
            assertNotNull(dao.findById(conn, 1));      // hit
            assertNotNull(dao.findByKey(conn, "key-1")); // hit, cached by key with the load
            assertEquals(2, dao.findByIds(conn, List.of(1L, 2L)).size()); // one hit, one miss, one load
            assertNotNull(dao.findById(conn, 3));      // miss, load, the cache of two pushes out an entry
        });
        assertEquals(3, counted.hits.get());
        assertEquals(3, counted.misses.get());
        assertEquals(3, counted.loads.size());
        assertTrue(counted.loads.stream().allMatch(nanos -> nanos >= 0));
        assertEquals(List.of(EvictionReason.LRU), counted.evictions);
        final CacheState state = dao.getCacheState();
        assertNotNull(state);
        assertEquals(2, state.size());
        assertEquals(2, state.maxSize());
        assertEquals(Duration.ofMinutes(5), state.ttl());
    }

    @Test
    void aDaoWithoutCacheReportsNothing() throws DatabaseError {
        final ArticleDao plain = collection.getArticleDao();
        final List<String> asked = new ArrayList<>();
        OrmMetrics.install(dao -> {
            asked.add(dao.getEntityClassName());
            return counted;
        });
        manager.executeTransaction(conn -> assertNotNull(plain.findById(conn, 1)));
        assertEquals(0, counted.hits.get() + counted.misses.get() + counted.loads.size());
        assertNull(plain.getCacheState());
        assertTrue(asked.isEmpty(), "without a cache, the DAO does not even ask for its metrics");
    }
}
