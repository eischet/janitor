// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The script method dao.queryForEach(sql, callback), which loads the entities for the IDs of a query and calls back for each of them. */
public class ArticleQueryForEachTestCase extends JanitorTest {

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

    /** Runs the script method and returns "key:id" for every callback, in order, followed by the returned count. */
    private List<String> queryForEach(final String sql) throws Exception {
        final List<String> calls = new ArrayList<>();
        final JanitorObject count = evaluate("return articles.queryForEach(sql, callback)", g -> {
            g.bind("articles", dao);
            g.bind("sql", sql);
            g.bindF("callback", (JCallable) (process, arguments) -> {
                calls.add(((Article) arguments.get(0)).getKey() + ":" + arguments.get(1).janitorGetHostValue());
                return Janitor.NULL;
            });
        });
        calls.add("count=" + count.janitorGetHostValue());
        return calls;
    }

    @Test
    void callsBackForEachEntityInTheOrderOfTheQuery() throws Exception {
        insertArticles(3);
        assertEquals(List.of("key-3:3", "key-1:1", "key-2:2", "count=3"),
                queryForEach("select article_id from demo_article order by case article_id when 3 then 0 else article_id end"));
    }

    @Test
    void worksAcrossSeveralChunks() throws Exception {
        insertArticles(250); // more than one chunk of entities per transaction
        final List<String> result = queryForEach("select article_id from demo_article order by article_id");
        assertEquals(251, result.size());
        assertEquals("key-1:1", result.get(0));
        assertEquals("key-100:100", result.get(99));
        assertEquals("key-101:101", result.get(100));
        assertEquals("key-250:250", result.get(249));
        assertEquals("count=250", result.get(250));
    }

    @Test
    void skipsIdentifiersWithoutEntityAndDoesNotCountThem() throws Exception {
        insertArticles(2);
        assertEquals(List.of("key-1:1", "key-2:2", "count=2"),
                queryForEach("select article_id from demo_article union all select 999 order by 1"));
    }
}
