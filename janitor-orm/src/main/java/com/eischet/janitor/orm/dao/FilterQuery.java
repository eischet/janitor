// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.janitor.orm.filter.FilterExpression;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a filter query for database operations.
 * This object is passed to {@link com.eischet.janitor.orm.dao.Dao#findByFilter(DatabaseConnection, FilterQuery)} for executing database queries with filtering and pagination.
 */
public class FilterQuery {

    protected @NotNull
    final FilterExpression filterExpression;
    protected @Nullable Integer queryTimeout;
    protected @Nullable Integer maxRows;
    protected @Nullable Integer offset;
    protected final @NotNull List<SortKey> sortKeys = new ArrayList<>();
    protected @Nullable String orderByClause;
    protected @Nullable Function<String, String> queryRewriter;

    /**
     * Create a new FilterQuery.
     *
     * @param filterExpression the expression to filter by
     */
    public FilterQuery(@NotNull final FilterExpression filterExpression) {
        this.filterExpression = filterExpression;
    }

    /**
     * Get the filter expression.
     *
     * @return the filter expression
     */
    @Contract(pure = true)
    public @NotNull FilterExpression getFilterExpression() {
        return filterExpression;
    }

    /**
     * Get the query timeout in seconds.
     *
     * @return the query timeout in seconds, or null if no timeout is set
     */
    public @Nullable Integer getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * Set the query timeout in seconds.
     *
     * @param queryTimeout the query timeout in seconds, or null to no timeout
     */
    @Contract(mutates = "this")
    public void setQueryTimeout(@Nullable final Integer queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    /**
     * Set the query timeout in seconds.
     *
     * @param queryTimeout the query timeout in seconds, or null to no timeout
     * @return this
     */
    @Contract(mutates = "this", value = "_ -> this")
    public FilterQuery withQueryTimeout(@Nullable final Integer queryTimeout) {
        setQueryTimeout(queryTimeout);
        return this;
    }

    /**
     * Get the maximum number of rows to return.
     *
     * @return the maximum number of rows to return, or null if no limit is set
     */
    @Contract(pure = true)
    public @Nullable Integer getMaxRows() {
        return maxRows;
    }

    /**
     * Set the maximum number of rows to return.
     *
     * @param maxRows the maximum number of rows to return, or null for no limit
     */
    @Contract(mutates = "this")
    public void setMaxRows(@Nullable final Integer maxRows) {
        this.maxRows = maxRows;
    }

    /**
     * Set the maximum number of rows to return.
     *
     * @param maxRows the maximum number of rows to return, or null for no limit
     * @return this
     */
    @Contract(mutates = "this", value = "_ -> this")
    public FilterQuery withMaxRows(@Nullable final Integer maxRows) {
        setMaxRows(maxRows);
        return this;
    }

    /**
     * Get the number of rows to skip, for paging.
     *
     * @return the number of rows to skip, or null if rows are not skipped
     */
    @Contract(pure = true)
    public @Nullable Integer getOffset() {
        return offset;
    }

    /**
     * Set the number of rows to skip, for paging. This requires {@link #setMaxRows max rows}: it is the size of the page, the offset is where it starts.
     * Setting an offset (even 0) also tells the DAO that this query is paged, so it makes the sort order unambiguous by appending the ID column
     * as the last sort criterion; without that, rows with equal sort values could move between pages.
     *
     * @param offset the number of rows to skip, or null for none; must not be negative
     */
    @Contract(mutates = "this")
    public void setOffset(@Nullable final Integer offset) {
        if (offset != null && offset < 0) {
            throw new IllegalArgumentException("the offset must not be negative");
        }
        this.offset = offset;
    }

    /**
     * Set the number of rows to skip, for paging; see {@link #setOffset(Integer)}.
     *
     * @param offset the number of rows to skip, or null for none
     * @return this
     */
    @Contract(mutates = "this", value = "_ -> this")
    public FilterQuery withOffset(@Nullable final Integer offset) {
        setOffset(offset);
        return this;
    }

    /**
     * Selects one page: sets {@link #setMaxRows max rows} to the page size and the {@link #setOffset offset} to the start of the page.
     *
     * @param pageIndex the number of the page, starting at 0
     * @param pageSize  the number of rows per page, at least 1
     * @return this
     */
    @Contract(mutates = "this", value = "_, _ -> this")
    public FilterQuery withPage(final int pageIndex, final int pageSize) {
        if (pageIndex < 0 || pageSize < 1) {
            throw new IllegalArgumentException("the page index must not be negative and the page size must be at least 1");
        }
        setMaxRows(pageSize);
        setOffset(Math.multiplyExact(pageIndex, pageSize));
        return this;
    }

    /**
     * Sorts by a field, ascending, after the criteria added before. The field is the name of the entity's field; the DAO maps it to the column
     * and rejects unknown names, so (unlike {@link #setOrderByClause}) no SQL from outside can get into the query this way.
     * Cannot be combined with {@link #setOrderByClause}.
     *
     * @param field the name of the field
     * @return this
     */
    @Contract(mutates = "this", value = "_ -> this")
    public FilterQuery orderBy(final @NotNull String field) {
        sortKeys.add(new SortKey(field, false));
        return this;
    }

    /**
     * Like {@link #orderBy(String)}, but descending.
     *
     * @param field the name of the field
     * @return this
     */
    @Contract(mutates = "this", value = "_ -> this")
    public FilterQuery orderByDescending(final @NotNull String field) {
        sortKeys.add(new SortKey(field, true));
        return this;
    }

    /**
     * @return the sort criteria added with {@link #orderBy(String)} and {@link #orderByDescending(String)}, in order
     */
    @Contract(pure = true)
    public @NotNull List<SortKey> getSortKeys() {
        return Collections.unmodifiableList(sortKeys);
    }

    /**
     * Get the optional ORDER BY clause.
     *
     * @return the ORDER BY clause, or null if no ORDER BY clause is set
     */
    public @Nullable String getOrderByClause() {
        return orderByClause;
    }

    /**
     * Set the optional ORDER BY clause.
     *
     * @param orderByClause the ORDER BY clause, or null to to use Dao's default. This text goes into the SQL as it is: never pass anything
     *                      that comes from a user or a script; use {@link #orderBy(String)} for that.
     */
    @Contract(mutates = "this")
    public void setOrderByClause(@Nullable final String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * Set the optional ORDER BY clause.
     *
     * @param orderByClause the ORDER BY clause, or null to to use Dao's default
     * @return this
     */
    @Contract(mutates = "this", value = "_ -> this")
    public FilterQuery withOrderByClause(final String orderByClause) {
        setOrderByClause(orderByClause);
        return this;
    }

    /**
     * Get the optional query rewriter.
     *
     * @return the query rewriter, or null if no rewriter is set
     */
    @Contract(pure = true)
    public @Nullable Function<String, String> getQueryRewriter() {
        return queryRewriter;
    }

    /**
     * Set the optional query rewriter.
     * <p>In the Dao, this function is called to replace the SQL query just before sending it off to the database.</p>
     * <p>The main use case for this is, in testing, to add things like "WAITFOR DELAY '00:00:35';", which is conveniently provided by MS SQL Server, when
     * testing the timeout of a query.</p>
     *
     * @param queryRewriter the query rewriter, or null to not rewrite the query
     */
    @Contract(mutates = "this")
    public void setQueryRewriter(@Nullable final Function<String, String> queryRewriter) {
        this.queryRewriter = queryRewriter;
    }

    /**
     * Set the optional query rewriter.
     *
     * @param queryRewriter the query rewriter, or null to not rewrite the query
     * @return this
     */
    @Contract(mutates = "this", value = "_ -> this")
    public FilterQuery withQueryRewriter(final Function<String, String> queryRewriter) {
        setQueryRewriter(queryRewriter);
        return this;
    }

    /**
     * Create a new FilterQuery from an existing expression.
     *
     * @param expression the expression to start from
     * @return a new FilterQuery
     */
    public static FilterQuery from(final FilterExpression expression) {
        return new FilterQuery(expression);
    }

    /**
     * Applies the query rewriter, if there is one, to a query.
     * @param query the SQL text
     * @return the rewritten SQL text
     */
    public String rewriteQuery(String query) {
        if (queryRewriter != null) {
            query = queryRewriter.apply(query);
        }
        return query;
    }

    /**
     * Applies the query rewriter, if there is one, to a query.
     * @param query the query
     * @return the rewritten query
     */
    public SelectStatement rewriteQuery(SelectStatement query) {
        if (queryRewriter != null) {
            query = SelectStatement.of(queryRewriter.apply(query.getSql()));
        }
        return query;
    }

}
