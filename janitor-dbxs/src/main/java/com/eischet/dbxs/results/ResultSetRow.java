/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.results;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;

public class ResultSetRow<T> {

    private final SimpleResultSet rs;
    private final T value;
    private int col = 0;

    public ResultSetRow(final SimpleResultSet rs, final T value) {
        this.rs = rs;
        this.value = value;
    }

    public ResultSetRow<T> readString(BiConsumer<T, String> consumer) throws SQLException {
        consumer.accept(value, rs.getString(++col));
        return this;
    }

    public ResultSetRow<T> readTimestampAsLocalDateTime(BiConsumer<T, LocalDateTime> consumer) throws SQLException {
        consumer.accept(value, date(rs.getTimestamp(++col)));
        return this;
    }

    protected LocalDateTime date(final Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public T getValue() {
        return value;
    }

}
