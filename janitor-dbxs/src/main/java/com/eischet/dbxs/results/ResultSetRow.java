// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs.results;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

/**
 * Reads the columns of the current row of a result set one after the other into the properties of an object.
 * @param <T> the type of the object
 */
public class ResultSetRow<T> {

    private final SimpleResultSet rs;
    private final T value;
    private int col = 0;

    public ResultSetRow(final @NotNull SimpleResultSet rs, final T value) {
        this.rs = rs;
        this.value = value;
    }

    /**
     * Reads the next column as a string, and passes it to the consumer together with the object.
     * @param consumer receives the object and the string
     * @return this row
     * @throws SQLException on database errors
     */
    @Contract("_ -> this")
    public @NotNull ResultSetRow<T> readString(@NotNull BiConsumer<T, String> consumer) throws SQLException {
        consumer.accept(value, rs.getString(++col));
        return this;
    }

    /**
     * Reads the next column as a timestamp, and passes it to the consumer as a local datetime together with the object.
     * @param consumer receives the object and the datetime
     * @return this row
     * @throws SQLException on database errors
     */
    @Contract("_ -> this")
    public @NotNull ResultSetRow<T> readTimestampAsLocalDateTime(BiConsumer<T, LocalDateTime> consumer) throws SQLException {
        consumer.accept(value, date(rs.getTimestamp(++col)));
        return this;
    }

    /**
     * @param timestamp the timestamp, may be null
     * @return the local datetime, or null if the timestamp is null
     */
    protected @Nullable LocalDateTime date(final @Nullable Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    /**
     * @return the object that the row was read into
     */
    public T getValue() {
        return value;
    }

}
