// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.ref;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.cache.SimpleEntityCache;
import com.eischet.janitor.orm.example.Article;
import com.eischet.janitor.orm.example.ArticleDao;
import com.eischet.janitor.orm.example.ExampleDaoCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A reference to an entity whose DAO keeps a cache asks the DAO each time, so it sees the entity as it was last saved; a reference to an
 * entity whose DAO has no cache keeps what it loaded first.
 */
public class ForeignKeyCachedTargetTestCase extends JanitorTest {

    /**
     * The example DAO with a cache.
     */
    static final class CachedArticleDao extends ArticleDao {
        CachedArticleDao(final ExampleDaoCollection collection) {
            super(collection);
            enableCache(new SimpleEntityCache<>(Duration.ofMinutes(5), 100));
        }
    }

    private SimpleDataManager manager;
    private ExampleDaoCollection collection;

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
            plain.insert(conn, article(0, "Before"));
        });
    }

    private Article article(final long id, final String name) {
        final Article article = new Article(collection);
        if (id > 0) {
            article.setId(id);
        }
        article.setKey("key-1");
        article.setShortCode("c1");
        article.setName(name);
        article.setStock(1);
        article.setActive(true);
        return article;
    }

    @Test
    void referencesToCachedEntitiesSeeTheirLatestSave() throws DatabaseError {
        final CachedArticleDao dao = new CachedArticleDao(collection);
        assertTrue(dao.cachesEntities());
        final ForeignKey<Article> byId = ForeignKeyInteger.createWithForce(1, dao);
        final ForeignKey<Article> byKey = ForeignKeyString.createWithForce("key-1", dao);
        assertEquals("Before", byId.resolveOrNull().getName());
        assertEquals("Before", byKey.resolveOrNull().getName());
        manager.executeTransaction(conn -> assertEquals("Before", byId.resolveOrNull(conn).getName()));

        manager.executeTransaction(conn -> dao.update(conn, article(1, "After")));

        assertEquals("After", byId.resolveOrNull().getName());
        assertEquals("After", byKey.resolveOrNull().getName());
        manager.executeTransaction(conn -> {
            assertEquals("After", byId.resolveOrNull(conn).getName());
            assertEquals("After", byKey.resolveOrNull(conn).getName());
        });
    }

    @Test
    void referencesToUncachedEntitiesKeepWhatTheyLoaded() throws DatabaseError {
        final ArticleDao dao = collection.getArticleDao();
        assertFalse(dao.cachesEntities());
        final ForeignKey<Article> byId = ForeignKeyInteger.createWithForce(1, dao);
        final Article first = byId.resolveOrNull();

        manager.executeTransaction(conn -> dao.update(conn, article(1, "After")));

        assertSame(first, byId.resolveOrNull(), "without a cache, the reference loads only once");
        assertEquals("Before", byId.resolveOrNull().getName());
    }
}
