package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.janitor.orm.filter.FilterExpression;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param orderByClause the ORDER BY clause, or null to to use Dao's default
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

    public String rewriteQuery(String query) {
        if (queryRewriter != null) {
            query = queryRewriter.apply(query);
        }
        return query;
    }

    public SelectStatement rewriteQuery(SelectStatement query) {
        if (queryRewriter != null) {
            query = SelectStatement.of(queryRewriter.apply(query.getSql()));
        }
        return query;
    }

}
