// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The script method dao.select(sql, parameters...), which returns the entities for the IDs of a query that the script wrote itself. */
public class ArticleSqlSelectTestCase extends JanitorTest {

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
        });
    }

    private void insertArticles(final int count) throws DatabaseError {
        manager.executeTransaction(conn -> {
            for (int i = 1; i <= count; i++) {
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

    /** Runs the script, which has the DAO as "articles", and returns the keys of the articles it returns, in their order. */
    private String run(final String script) throws Exception {
        return ((JList) evaluate(script, g -> g.bind("articles", dao))).stream().map(article -> ((Article) article).getKey()).toList().toString();
    }

    @Test
    void returnsTheEntitiesInTheOrderOfTheQuery() throws Exception {
        insertArticles(3);
        assertEquals("[key-3, key-1, key-2]", run("""
                return articles.select("select article_id from demo_article order by case article_id when 3 then 0 else article_id end")"""));
    }

    @Test
    void bindsParameters() throws Exception {
        insertArticles(5);
        assertEquals("[key-4, key-5]", run("""
                return articles.select("select article_id from demo_article where stock > ? and article_name like ? order by article_id", 3, "Article %")"""));
        assertEquals("[]", run("""
                return articles.select("select article_id from demo_article where short_code = ?", "nope")"""));
        assertEquals("[]", run("""
                return articles.select("select article_id from demo_article where short_code = ?", null)"""));
    }

    @Test
    void worksWithManyEntities() throws Exception {
        insertArticles(1200); // more than one IN list of IDs
        final JList found = (JList) evaluate("return articles.select(\"select article_id from demo_article order by article_id desc\")", g -> g.bind("articles", dao));
        assertEquals(1200, found.size());
        assertEquals("key-1200", ((Article) found.get(0)).getKey());
        assertEquals("key-1", ((Article) found.get(1199)).getKey());
    }

    @Test
    void skipsIdentifiersWithoutEntity() throws Exception {
        insertArticles(2);
        assertEquals("[key-1, key-2]", run("""
                return articles.select("select article_id from demo_article union all select 999 order by 1")"""));
    }

    @Test
    void lambdasStillWorkAndOtherArgumentsAreRefused() throws Exception {
        insertArticles(3);
        assertEquals("[key-2]", run("return articles.select(a -> a.stock == 2)"));
        final JanitorRuntimeException refused = assertThrows(JanitorRuntimeException.class, () -> run("return articles.select(17)"));
        assertTrue(refused.getMessage().contains("SQL"), refused.getMessage());
    }

}
