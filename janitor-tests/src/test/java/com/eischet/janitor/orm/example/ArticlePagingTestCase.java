// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.example;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.dialects.DatabaseDialectH2;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.dao.FilterQuery;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.filter.MalformedExpression;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Limit, offset and sort order of {@code findByFilter} and {@code findAll}. Everything runs twice: against a dialect that limits rows in SQL, and
 * against one that cannot, where limit and offset are applied while reading.
 */
public class ArticlePagingTestCase extends JanitorTest {

    /** H2, pretending to be a database that cannot limit rows in SQL. */
    static class NoLimitDialect extends DatabaseDialectH2 {
        @Override
        public boolean canLimitAndOffset(final DatabaseVersion databaseVersion) {
            return false;
        }
    }

    private SimpleDataManager manager;
    private ArticleDao sqlLimit;
    private ArticleDao readingLimit;

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        final ExampleDaoCollection modern = new ExampleDaoCollection(manager);
        final ExampleDaoCollection legacy = new ExampleDaoCollection(manager, new NoLimitDialect());
        sqlLimit = modern.getArticleDao();
        readingLimit = legacy.getArticleDao();
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
            for (int i = 1; i <= 25; i++) {
                final Article article = new Article(modern);
                article.setKey("key-" + i);
                article.setShortCode("c" + i);
                article.setName("Article " + (i % 3)); // many rows with the same name: ties must not move between pages
                article.setStock(i);
                article.setActive(true);
                sqlLimit.insert(conn, article);
            }
        });
    }

    private static FilterExpression all() {
        return FilterExpression.from("name", FilterOperator.CONTAINS, "Article");
    }

    private List<String> keys(final ArticleDao dao, final FilterQuery query) throws DatabaseError {
        return manager.callTransaction(conn -> dao.findByFilter(conn, query)).stream().map(Article::getKey).toList();
    }

    /** The sort order and limit clause of a query: everything from "order by" on. */
    private static String orderByPart(final String sql) {
        return sql.substring(sql.indexOf("order by"));
    }

    private static List<String> keyRange(final int fromInclusive, final int toInclusive) {
        final List<String> keys = new ArrayList<>();
        for (int i = fromInclusive; i <= toInclusive; i++) {
            keys.add("key-" + i);
        }
        return keys;
    }

    @Test
    void sortKeysSortByFieldAndDirection() throws DatabaseError {
        for (final ArticleDao dao : List.of(sqlLimit, readingLimit)) {
            assertEquals(List.of("key-25", "key-24", "key-23"), keys(dao, FilterQuery.from(all()).orderByDescending("stock").withMaxRows(3)));
            assertEquals(List.of("key-1", "key-2", "key-3"), keys(dao, FilterQuery.from(all()).orderBy("stock").withMaxRows(3)));
        }
    }

    @Test
    void maxRowsIsHonoredEvenIfTheDatabaseCannotLimit() throws DatabaseError {
        assertEquals(7, keys(readingLimit, FilterQuery.from(all()).orderBy("stock").withMaxRows(7)).size());
        assertEquals(25, keys(readingLimit, FilterQuery.from(all()).orderBy("stock")).size());
    }

    @Test
    void pagesDoNotOverlapAndCoverEverythingEvenForTiedSortValues() throws DatabaseError {
        for (final ArticleDao dao : List.of(sqlLimit, readingLimit)) {
            final List<String> seen = new ArrayList<>();
            for (int page = 0; page < 3; page++) {
                final List<String> keys = keys(dao, FilterQuery.from(all()).orderBy("name").withPage(page, 10));
                assertEquals(page < 2 ? 10 : 5, keys.size());
                seen.addAll(keys);
            }
            assertEquals(25, seen.size());
            assertEquals(25, seen.stream().distinct().count());
            // tie-breaker is the ID, which is also the order of insertion
            assertEquals(List.of("key-3", "key-6", "key-9", "key-12", "key-15", "key-18", "key-21", "key-24"), seen.subList(0, 8)); // all "Article 0"
        }
    }

    @Test
    void offsetWorksWithoutExplicitSortOrder() throws DatabaseError {
        for (final ArticleDao dao : List.of(sqlLimit, readingLimit)) {
            final List<String> page1 = keys(dao, FilterQuery.from(all()).withPage(0, 10));
            final List<String> page2 = keys(dao, FilterQuery.from(all()).withPage(1, 10));
            assertEquals(10, page1.size());
            assertEquals(10, page2.size());
            assertTrue(page1.stream().noneMatch(page2::contains));
        }
    }

    @Test
    void paginationAddsTheIdAsLastSortCriterionOnlyWhenPaging() {
        assertEquals("order by article_name asc, article_id asc LIMIT ? OFFSET ?", orderByPart(sqlLimit.sqlFor(FilterQuery.from(all()).orderBy("name").withPage(1, 10))));
        assertEquals("order by article_name asc LIMIT ? OFFSET ?", orderByPart(sqlLimit.sqlFor(FilterQuery.from(all()).orderBy("name").withMaxRows(10))));
        assertEquals("order by article_key LIMIT ? OFFSET ?", orderByPart(sqlLimit.sqlFor(FilterQuery.from(all()).withMaxRows(10))));
        assertEquals("order by article_key, article_id LIMIT ? OFFSET ?", orderByPart(sqlLimit.sqlFor(FilterQuery.from(all()).withPage(0, 10))));
    }

    @Test
    void anExplicitSortByIdIsNotRepeated() {
        assertEquals("order by article_id asc LIMIT ? OFFSET ?", orderByPart(sqlLimit.sqlFor(FilterQuery.from(all()).orderBy("id").withPage(0, 10))));
    }

    @Test
    void findAllPagesToo() throws DatabaseError {
        for (final ArticleDao dao : List.of(sqlLimit, readingLimit)) {
            final List<String> keys = manager.callTransaction(conn -> dao.findAll(conn, 5, 10)).stream().map(Article::getKey).toList();
            assertEquals(5, keys.size());
            assertEquals(5, manager.callTransaction(conn -> dao.findAll(conn, 5)).size());
            assertEquals(25, manager.callTransaction(conn -> dao.findAll(conn)).size());
            final List<String> secondPage = manager.callTransaction(conn -> dao.findAll(conn, 5, 15)).stream().map(Article::getKey).toList();
            assertTrue(keys.stream().noneMatch(secondPage::contains));
        }
    }

    @Test
    void offsetBeyondTheEndYieldsNothing() throws DatabaseError {
        for (final ArticleDao dao : List.of(sqlLimit, readingLimit)) {
            assertEquals(List.of(), keys(dao, FilterQuery.from(all()).withPage(5, 10)));
        }
    }

    @Test
    void invalidInputIsRejected() {
        assertThrows(MalformedExpression.class, () -> sqlLimit.sqlFor(FilterQuery.from(all()).orderBy("nonexistent; drop table x")));
        assertThrows(MalformedExpression.class, () -> sqlLimit.sqlFor(FilterQuery.from(all()).orderBy("name").withOrderByClause("order by 1")));
        assertThrows(IllegalArgumentException.class, () -> sqlLimit.sqlFor(FilterQuery.from(all()).withOffset(5))); // an offset needs a limit
        assertThrows(IllegalArgumentException.class, () -> FilterQuery.from(all()).withOffset(-1));
        assertThrows(IllegalArgumentException.class, () -> FilterQuery.from(all()).withPage(0, 0));
        assertThrows(IllegalArgumentException.class, () -> manager.callTransaction(conn -> sqlLimit.findAll(conn, null, 5)));
    }

    @Test
    void rawOrderByClauseStillWorks() throws DatabaseError {
        assertEquals(keyRange(1, 3), keys(sqlLimit, FilterQuery.from(all()).withOrderByClause("order by stock").withMaxRows(3)));
    }
}
