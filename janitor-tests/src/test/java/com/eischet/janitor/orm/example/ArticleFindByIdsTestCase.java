// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@code findByIds} loads many entities with few queries. */
public class ArticleFindByIdsTestCase extends JanitorTest {

    private ExampleDaoCollection collection;
    private ArticleDao dao;
    private SimpleDataManager manager;

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        collection = new ExampleDaoCollection(manager);
        dao = collection.getArticleDao();
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
            for (int i = 1; i <= 1200; i++) {
                final Article article = new Article(collection);
                article.setKey("key-" + i);
                article.setShortCode("c" + i);
                article.setName("Article " + i);
                article.setStock(i);
                article.setActive(true);
                dao.insert(conn, article);
            }
        });
    }

    private List<String> keys(final Long... ids) throws DatabaseError {
        return manager.callTransaction(conn -> dao.findByIds(conn, Arrays.asList(ids))).stream().map(Article::getKey).toList();
    }

    @Test
    void returnsEntitiesInTheOrderOfTheIds() throws DatabaseError {
        assertEquals(List.of("key-3", "key-1", "key-2"), keys(3L, 1L, 2L));
    }

    @Test
    void skipsMissingDuplicateAndNullIds() throws DatabaseError {
        assertEquals(List.of("key-2", "key-1"), keys(2L, 999999L, null, 1L, 2L));
    }

    @Test
    void emptyInputDoesNotQuery() throws DatabaseError {
        assertEquals(List.of(), keys());
        assertEquals(List.of(), keys((Long) null));
    }

    @Test
    void loadsMoreIdsThanFitIntoOneInList() throws DatabaseError {
        final List<Long> ids = new ArrayList<>();
        for (long id = 1200; id >= 1; id--) {
            ids.add(id); // descending, over three chunks of 500
        }
        final List<Article> found = manager.callTransaction(conn -> dao.findByIds(conn, ids));
        assertEquals(1200, found.size());
        assertEquals("key-1200", found.get(0).getKey());
        assertEquals("key-1", found.get(1199).getKey());
        assertTrue(found.stream().allMatch(a -> a.getName().equals("Article " + a.getKey().substring(4))));
    }

    @Test
    void readsAllColumns() throws DatabaseError {
        final Article article = manager.callTransaction(conn -> dao.findByIds(conn, List.of(7L))).getFirst();
        assertEquals(7, article.getStock());
        assertEquals("c7", article.getShortCode());
        assertTrue(article.isActive());
    }

    @Test
    void isAvailableToScripts() throws Exception {
        final JanitorObject result = evaluate("return articles.findByIds([3, 1, 999999, null, 3, 2])", g -> g.bind("articles", dao));
        assertEquals(List.of("key-3", "key-1", "key-2"),
                ((JList) result).janitorGetHostValue().stream().map(o -> ((Article) o).getKey()).toList());
    }

    @Test
    void scriptsMustPassNumbers() {
        assertThrows(JanitorRuntimeException.class, () -> evaluate("return articles.findByIds(['a'])", g -> g.bind("articles", dao)));
    }
}
