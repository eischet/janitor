package com.eischet.janitor.orm.dao;

import com.eischet.janitor.orm.filter.FilterExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterQuery {

    protected @NotNull final FilterExpression filterExpression;
    protected @Nullable Integer queryTimeout;
    protected @Nullable Integer maxRows;
    protected @Nullable String orderByClause;

    public FilterQuery(@NotNull final FilterExpression filterExpression) {
        this.filterExpression = filterExpression;
    }

    public void setQueryTimeout(@Nullable final Integer queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setMaxRows(@Nullable final Integer maxRows) {
        this.maxRows = maxRows;
    }

    public @Nullable String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(@Nullable final String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public @NotNull FilterExpression getFilterExpression() {
        return filterExpression;
    }

    public @Nullable Integer getQueryTimeout() {
        return queryTimeout;
    }

    public @Nullable Integer getMaxRows() {
        return maxRows;
    }

    public FilterQuery withQueryTimeout(@Nullable final Integer queryTimeout) {
        setQueryTimeout(queryTimeout);
        return this;
    }

    public FilterQuery withMaxRows(@Nullable final Integer maxRows) {
        setMaxRows(maxRows);
        return this;
    }

    public FilterQuery withOrderByClause(final String orderByClause) {
        setOrderByClause(orderByClause);
        return this;
    }

    public FilterQuery from(final FilterExpression expression) {
        return new FilterQuery(expression);
    }

}
