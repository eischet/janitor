/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.results;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

public class ResultSetRow<T> {

    private final SimpleResultSet rs;
    private final T value;
    private int col = 0;

    public ResultSetRow(final @NotNull SimpleResultSet rs, final T value) {
        this.rs = rs;
        this.value = value;
    }

    @Contract("_ -> this")
    public @NotNull ResultSetRow<T> readString(@NotNull BiConsumer<T, String> consumer) throws SQLException {
        consumer.accept(value, rs.getString(++col));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ResultSetRow<T> readTimestampAsLocalDateTime(BiConsumer<T, LocalDateTime> consumer) throws SQLException {
        consumer.accept(value, date(rs.getTimestamp(++col)));
        return this;
    }

    protected @Nullable LocalDateTime date(final @Nullable Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public T getValue() {
        return value;
    }

}
