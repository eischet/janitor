// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.dao.FilterQuery;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs filters through the example entity against an in-memory H2 database: CRUD basics, case sensitivity of
 * text filters, the effect of the column case hint on the generated SQL, and LIKE wildcards in search values.
 * (H2 compares case-sensitively, like Oracle, which is what makes it a good stand-in for these checks.)
 */
public class ArticleFilterTestCase extends JanitorTest {

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
            insert(conn, "Widget-A", "WID-A", "Blue Widget", "100% cotton", 5, true);
            insert(conn, "widget-b", "WID-B", "Red widget", null, 0, false);
            insert(conn, "Gadget", "GAD", "Gadget Deluxe", "a_b", 12, true);
        });
    }

    private void insert(final com.eischet.dbxs.DatabaseConnection conn, final String key, final String shortCode, final String name, final String description, final int stock, final boolean active) throws DatabaseError {
        final Article article = new Article(collection);
        article.setKey(key);
        article.setShortCode(shortCode);
        article.setName(name);
        article.setDescription(description);
        article.setStock(stock);
        article.setActive(active);
        dao.insert(conn, article);
    }

    private List<String> keys(final FilterExpression expression) throws DatabaseError {
        return manager.callTransaction(conn -> dao.findByFilter(conn, FilterQuery.from(expression).withOrderByClause("order by article_key")))
                .stream().map(Article::getKey).toList();
    }

    private String sql(final FilterExpression expression) {
        return dao.sqlFor(FilterQuery.from(expression).withOrderByClause("order by article_key"));
    }

    @Test
    void insertAndFindById() throws DatabaseError {
        final Article article = manager.callTransaction(conn -> dao.findById(conn, 1));
        assertNotNull(article);
        assertEquals("Widget-A", article.getKey());
        assertEquals("WID-A", article.getShortCode());
        assertEquals("100% cotton", article.getDescription());
        assertEquals(5, article.getStock());
        assertTrue(article.isActive());
    }

    @Test
    void textSearchIgnoresCaseByDefault() throws DatabaseError {
        assertEquals(List.of("Widget-A", "widget-b"), keys(FilterExpression.from("name", FilterOperator.CONTAINS, "WIDGET")));
        assertEquals(List.of("Widget-A"), keys(FilterExpression.from("name", FilterOperator.STARTSWITH, "blue")));
        assertEquals(List.of("Gadget"), keys(FilterExpression.from("name", FilterOperator.ENDSWITH, "DELUXE")));
        assertEquals(List.of("Gadget"), keys(FilterExpression.from("name", FilterOperator.DOESNOTCONTAIN, "widget")));
    }

    @Test
    void textSearchCanBeMadeCaseSensitive() throws DatabaseError {
        assertEquals(List.of(), keys(FilterExpression.from("name", FilterOperator.CONTAINS, false, "WIDGET")));
        assertEquals(List.of("widget-b"), keys(FilterExpression.from("name", FilterOperator.CONTAINS, false, "widget")));
    }

    @Test
    void comparisonsAreExactUnlessAskedOtherwise() throws DatabaseError {
        assertEquals(List.of(), keys(FilterExpression.from("key", FilterOperator.EQ, "WIDGET-A")));
        assertEquals(List.of("Widget-A"), keys(FilterExpression.from("key", FilterOperator.EQ, true, "WIDGET-A")));
        assertEquals(List.of("Gadget", "widget-b"), keys(FilterExpression.from("key", FilterOperator.NEQ, true, "widget-a")));
    }

    @Test
    void mixedCaseColumnsAreFoldedInTheDatabase() {
        assertTrue(sql(FilterExpression.from("name", FilterOperator.CONTAINS, "x")).contains("lower(article_name) like lower(?)"));
        assertTrue(sql(FilterExpression.from("key", FilterOperator.EQ, true, "x")).contains("lower(article_key) = lower(?)"));
    }

    @Test
    void upperCaseColumnsNormalizeTheValueInsteadOfTheColumn() throws DatabaseError {
        final FilterExpression startsWith = FilterExpression.from("shortCode", FilterOperator.STARTSWITH, "wid");
        assertEquals(List.of("Widget-A", "widget-b"), keys(startsWith));
        assertFalse(sql(startsWith).contains("lower("), "the column must stay bare: " + sql(startsWith));
        assertTrue(sql(startsWith).contains("short_code like ?"));

        final FilterExpression equals = FilterExpression.from("shortCode", FilterOperator.EQ, true, "wid-a");
        assertEquals(List.of("Widget-A"), keys(equals));
        assertFalse(sql(equals).contains("lower("), "the column must stay bare: " + sql(equals));

        // without ignoreCase, an equality comparison stays exact
        assertEquals(List.of(), keys(FilterExpression.from("shortCode", FilterOperator.EQ, "wid-a")));
        // and a case-sensitive text search on an upper-case column is passed through as is
        assertEquals(List.of(), keys(FilterExpression.from("shortCode", FilterOperator.STARTSWITH, false, "wid")));
        assertEquals(List.of("Widget-A", "widget-b"), keys(FilterExpression.from("shortCode", FilterOperator.STARTSWITH, false, "WID")));
    }

    @Test
    void wildcardsInSearchValuesAreMatchedLiterally() throws DatabaseError {
        assertEquals(List.of("Widget-A"), keys(FilterExpression.from("description", FilterOperator.CONTAINS, "100%")));
        assertEquals(List.of(), keys(FilterExpression.from("description", FilterOperator.CONTAINS, "%%")));
        assertEquals(List.of("Gadget"), keys(FilterExpression.from("description", FilterOperator.CONTAINS, "a_b")));
        assertEquals(List.of(), keys(FilterExpression.from("description", FilterOperator.CONTAINS, "axb")));
    }

    @Test
    void emptyAndCount() throws DatabaseError {
        assertEquals(List.of("widget-b"), keys(new FilterExpression("description", FilterOperator.ISEMPTY)));
        final int count = manager.callTransaction(conn -> dao.countByFilter(conn, FilterExpression.from("name", FilterOperator.CONTAINS, "WIDGET")));
        assertEquals(2, count);
    }
}
