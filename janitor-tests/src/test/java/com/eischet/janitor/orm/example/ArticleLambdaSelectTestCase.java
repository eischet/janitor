// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The script methods dao.select(lambda) and dao.count(lambda), which run a lambda as a query, see LambdaFilter. */
public class ArticleLambdaSelectTestCase extends JanitorTest {

    private ExampleDaoCollection collection;
    private ArticleDao dao;

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
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
            insert(conn, "Widget-A", "WID-A", "Blue Widget", "100% cotton", 5, true);
            insert(conn, "widget-b", "WID-B", "Red widget", null, 0, false);
            insert(conn, "Gadget", "GAD", "Gadget Deluxe", "a_b", 12, true);
        });
    }

    private void insert(final DatabaseConnection conn, final String key, final String shortCode, final String name, final String description, final int stock, final boolean active) throws DatabaseError {
        final Article article = new Article(collection);
        article.setKey(key);
        article.setShortCode(shortCode);
        article.setName(name);
        article.setDescription(description);
        article.setStock(stock);
        article.setActive(active);
        dao.insert(conn, article);
    }

    /** Runs the script, which returns the result of articles.select(...), and returns the keys of the articles, sorted. */
    private List<String> keys(final String script) throws Exception {
        final JanitorObject result = evaluate(script, g -> g.bind("articles", dao));
        return ((JList) result).stream().map(article -> ((Article) article).getKey()).sorted().toList();
    }

    @Test
    void equality() throws Exception {
        assertEquals(List.of("Gadget"), keys("return articles.select(a -> a.shortCode == \"GAD\")"));
        assertEquals(List.of("Gadget", "widget-b"), keys("return articles.select(a -> a.shortCode != \"WID-A\")"));
    }

    @Test
    void capturedValuesAndOperandsInEitherOrder() throws Exception {
        assertEquals(List.of("Gadget", "Widget-A"), keys("min = 4; return articles.select(a -> min < a.stock)"));
        assertEquals(List.of("Gadget"), keys("function codes(c) { return articles.select(a -> a.stock >= c * 2); } return codes(6)"));
    }

    @Test
    void logic() throws Exception {
        assertEquals(List.of("Widget-A"), keys("return articles.select(a -> a.active && not (a.stock > 10))"));
        assertEquals(List.of("Gadget", "widget-b"), keys("return articles.select(a -> a.stock == 0 or a.stock > 10)"));
        assertEquals(List.of("Gadget"), keys("return articles.select(a -> not (a.stock < 10 || !a.active))"));
    }

    @Test
    void inList() throws Exception {
        assertEquals(List.of("Gadget", "widget-b"), keys("return articles.select(a -> a.shortCode in [\"GAD\", \"WID-B\"])"));
        assertEquals(List.of("Widget-A"), keys("codes = [\"GAD\", \"WID-B\"]; return articles.select(a -> a.shortCode not in codes)"));
    }

    @Test
    void textIsCaseSensitiveAsInScripts() throws Exception {
        assertEquals(List.of("widget-b"), keys("return articles.select(a -> a.name.startsWith(\"Red\"))"));
        assertEquals(List.of(), keys("return articles.select(a -> a.name.startsWith(\"red\"))"));
        assertEquals(List.of(), keys("return articles.select(a -> a.key == \"WIDGET-A\")"));
        assertEquals(List.of("Widget-A"), keys("return articles.select(a -> a.key ~= \"WIDGET-A\")"));
        assertEquals(List.of("Widget-A", "widget-b"), keys("return articles.select(a -> a.name.containsIgnoreCase(\"WIDGET\"))"));
        assertEquals(List.of("Widget-A"), keys("return articles.select(a -> a.name ~ \"*Widget\")"));
    }

    @Test
    void nullAndBooleans() throws Exception {
        assertEquals(List.of("widget-b"), keys("return articles.select(a -> a.description == null)"));
        assertEquals(List.of("Gadget", "Widget-A"), keys("return articles.select(a -> null != a.description)"));
        assertEquals(List.of("widget-b"), keys("return articles.select(a -> not a.active)"));
    }

    @Test
    void count() throws Exception {
        assertEquals(2L, evaluate("return articles.count(a -> a.active)", g -> g.bind("articles", dao)).janitorGetHostValue());
    }

    @Test
    void rejectsWhatCannotBeAQuery() {
        assertRejected("return articles.select(a -> a.unknown == 1)", "'unknown' is not a field");
        assertRejected("return articles.select(a -> a.stock * 2 > 4)", "expected a property of the parameter");
        assertRejected("return articles.select(a -> { x = a.stock; return x > 1; })", "single expression");
        assertRejected("return articles.select((a, b) -> a.stock > 1)", "exactly one parameter");
        assertRejected("return articles.select(\"stock > 1\")", "expecting a lambda");
        assertRejected("return articles.select(a -> a.stock in [])", "at least one value");
    }

    private void assertRejected(final String script, final String messagePart) {
        final JanitorArgumentException e = assertThrows(JanitorArgumentException.class, () -> evaluate(script, g -> g.bind("articles", dao)));
        assertTrue(e.getMessage().contains(messagePart), e.getMessage());
    }

}
