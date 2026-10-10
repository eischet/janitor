/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.statements.GenericStatement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A wrapper around a JDBC {@link PreparedStatement} with a fluent interface for setting parameters.
 * Parameters are set one after the other, each {@code add} call binds the next parameter.
 * The wrapper also remembers the parameters, for logging and diagnostics.
 */
public class SimplePreparedStatement {

    final List<Arg> args = new LinkedList<>();
    final PreparedStatement ps;
    private final DatabaseDialect dialect;
    private final GenericStatement statement;
    int col = 0;

    public SimplePreparedStatement(final DatabaseDialect dialect, @NotNull final GenericStatement statement, @NotNull final PreparedStatement ps) {
        this.dialect = dialect;
        this.statement = statement;
        this.ps = ps;
    }

    /**
     * Sets the query timeout.
     * @param seconds the timeout in seconds
     * @return this statement
     * @throws SQLException on database errors
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement setQueryTimeout(final int seconds) throws SQLException {
        this.ps.setQueryTimeout(seconds);
        return this;
    }

    /**
     * Limits the number of rows that the query returns.
     * @param maxRows the maximum number of rows
     * @return this statement
     * @throws SQLException on database errors
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement setMaxRows(final int maxRows) throws SQLException {
        this.ps.setMaxRows(maxRows); // theoretically there's a long variant of this, but who would load more than Integer.MAX_VALUE rows in one go?
        return this;
    }

    /**
     * @return the index of the parameter that was set last
     */
    public int getLatestColumnIndex() {
        return col;
    }

    /**
     * @return the statement that this prepared statement was created from
     */
    public GenericStatement getStatement() {
        return statement;
    }

    /**
     * Binds a nullable long to the next parameter; null is bound as an SQL NULL.
     * @param v the value, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addLongInstance(final Long v) throws SQLException {
        if (v == null) {
            return addNullInteger();
        } else {
            final int i = ++col;
            args.add(new Arg(col, v, "long"));
            ps.setLong(i, v);
            return this;
        }
    }

    /**
     * Binds a nullable long to the next parameter; null is bound as an SQL NULL.
     * @param v the value, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addLong(final Long v) throws SQLException {
        if (v == null) {
            return addNullInteger();
        } else {
            final int i = ++col;
            args.add(new Arg(col, v, "long"));
            ps.setLong(i, v);
            return this;
        }
    }

    /**
     * Binds a nullable integer to the next parameter; null is bound as an SQL NULL.
     * @param v the value, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addInt(final Integer v) throws SQLException {
        if (v == null) {
            return addNullInteger();
        } else {
            final int i = ++col;
            args.add(new Arg(col, v, "int"));
            ps.setInt(i, v);
            return this;
        }
    }

    /**
     * Binds a nullable integer from a value source to the next parameter.
     * @param source supplies the value
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addIntFrom(final @NotNull ValueSource<Integer> source) throws SQLException {
        return addInt(source.getValue());
    }

    /**
     * Binds an SQL NULL to the next parameter.
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("-> this")
    public SimplePreparedStatement addNull() throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, "NULL", "null"));
        ps.setNull(i, Types.NULL);
        return this;
    }

    /**
     * Binds a double to the next parameter.
     * @param v the value
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addDouble(final double v) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, v, "double"));
        ps.setDouble(i, v);
        return this;
    }

    /**
     * Binds a nullable double to the next parameter; null is bound as an SQL NULL.
     * @param v the value, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addDoubleInstance(final Double v) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, v, "Double"));
        if (v == null) {
            ps.setNull(i, Types.DOUBLE);
        } else {
            ps.setDouble(i, v);
        }
        return this;
    }

    /**
     * Binds a nullable double from a value source to the next parameter.
     * @param source supplies the value
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addDoubleInstanceFrom(final @NotNull ValueSource<Double> source) throws SQLException {
        return addDoubleInstance(source.getValue());
    }

    /**
     * Binds several integers to the next parameters, one for each value.
     * @param values the values
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addInts(final Integer... values) throws SQLException {
        for (final Integer value : values) {
            addInt(value);
        }
        return this;
    }

    /**
     * Binds a string to the next parameter.
     * @param v the value, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addString(final String v) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, v, "string"));
        ps.setString(i, v);
        return this;
    }

    /**
     * Binds a string from a value source to the next parameter.
     * @param source supplies the value
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addStringFrom(final @NotNull ValueSource<String> source) throws SQLException {
        return addString(source.getValue());
    }


    /**
     * Binds several strings to the next parameters, one for each value.
     * @param values the values
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addStrings(final String... values) throws SQLException {
        for (final String value : values) {
            addString(value);
        }
        return this;
    }

    /**
     * Binds a long to the next parameter.
     * @param v the value
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addLong(final long v) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, v, "long"));
        ps.setLong(i, v);
        return this;
    }

    /**
     * Binds a long from a value source to the next parameter.
     * @param source supplies the value
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addLongFrom(final @NotNull ValueSource<Long> source) throws SQLException {
        return addLong(source.getValue());
    }

    /**
     * Binds a legacy {@link Date} to the next parameter, as an SQL date; null is bound as an SQL NULL.
     * @param date the date, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addDate(final Date date) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, date, "Date"));
        if (date != null) {
            ps.setDate(i, new java.sql.Date(date.getTime()));
        } else {
            ps.setDate(i, null);
        }
        return this;
    }

    /**
     * Binds a nullable local date to the next parameter.
     * @param date the date, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addNullableDate(final @Nullable LocalDate date) throws SQLException {
        final int i = ++col;
        if (date != null) {
            ps.setObject(i, date);
            args.add(new Arg(col, date, "LocalDate"));
        } else {
            ps.setNull(i, Types.DATE);
            args.add(new Arg(col, null, "LocalDate"));
        }
        return this;
    }

    /**
     * Binds a nullable local datetime to the next parameter.
     * @param date the datetime, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addNullableDateTime(final @Nullable LocalDateTime date) throws SQLException {
        final int i = ++col;
        if (date != null) {
            ps.setObject(i, date);
            args.add(new Arg(col, date, "LocalDateTime"));
        } else {
            ps.setNull(i, Types.TIMESTAMP);
            args.add(new Arg(col, null, "LocalDateTime"));
        }
        return this;
    }

    /**
     * Binds a nullable double to the next parameter.
     * @param value the value, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addNullableDouble(final @Nullable Double value) throws SQLException {
        final int i = ++col;
        if (value != null) {
            ps.setObject(i, value);
            args.add(new Arg(col, value, "Double"));
        } else {
            ps.setNull(i, Types.DOUBLE);
            args.add(new Arg(col, null, "Double"));
        }
        return this;
    }


    /**
     * Binds a local date to the next parameter, as a timestamp at the start of the day.
     * @param date the date
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addDate(final @NotNull LocalDate date) throws SQLException {
        return addTimestamp(date.atStartOfDay());
    }

    /**
     * Binds a local date to the next parameter, as a timestamp at the start of the day.
     * @param timestamp the date, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addTimestamp(final LocalDate timestamp) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, timestamp, "timestamp(ldt)"));
        ps.setTimestamp(i, timestamp(timestamp));
        return this;
    }

    /**
     * Binds a local datetime to the next parameter, as a timestamp.
     * @param timestamp the datetime, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addTimestamp(final LocalDateTime timestamp) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, timestamp, "timestamp(ldt)"));
        ps.setTimestamp(i, timestamp(timestamp));
        return this;
    }

    /**
     * Binds a local datetime to the next parameter, as a timestamp.
     * @param timestamp the datetime, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addLocalDateTime(final LocalDateTime timestamp) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, timestamp, "timestamp(ldt)"));
        ps.setTimestamp(i, timestamp(timestamp));
        return this;
    }

    /**
     * Binds a local datetime from a value source to the next parameter.
     * @param source supplies the value
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addLocalDateTimeFrom(final @NotNull ValueSource<LocalDateTime> source) throws SQLException {
        return addLocalDateTime(source.getValue());
    }

    @Nullable
    private Timestamp timestamp(final LocalDate dt) {
        return dt == null ? null : Timestamp.valueOf(dt.atStartOfDay());
    }

    @Nullable
    private Timestamp timestamp(final LocalDateTime dt) {
        return dt == null ? null : Timestamp.valueOf(dt);
    }

    /**
     * Binds a timestamp to the next parameter.
     * @param timestamp the timestamp, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addTimestamp(final Timestamp timestamp) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, timestamp, "timestamp"));
        ps.setTimestamp(i, timestamp);
        return this;
    }

    /**
     * Binds a long text in a national character set (NCLOB) to the next parameter; null is bound as an empty text.
     * @param clob the text, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addNationalClob(final String clob) throws SQLException {
        // LATER: NCLOB probably needs to be handled differently from CLOB!
        return addClob(new StringReader(clob == null ? "" : clob));
    }

    /**
     * Binds a long text (CLOB) to the next parameter; null is bound as an empty text.
     * @param clob the text, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addClob(final String clob) throws SQLException {
        return addClob(new StringReader(clob == null ? "" : clob));
    }

    /**
     * Binds a long text (CLOB) to the next parameter, handling database-specific quirks through the dialect.
     * @param clob the text
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addClob(final StringReader clob) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, clob, "clob"));
        dialect.addClobToStatement(ps, i, clob);
        return this;
    }

    /**
     * Binds binary data to the next parameter, using {@code setBytes}; null is bound as an SQL NULL.
     * @param data the data, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addBytes(final byte[] data) throws SQLException {
        // LATER: this is for SQLite only!
        final int i = ++col;
        args.add(new Arg(col, data, "blob"));
        if (data == null) {
            ps.setNull(i, Types.BLOB);
        } else {
            ps.setBytes(i, data);
        }
        return this;
    }

    /**
     * Binds binary data (BLOB) to the next parameter, using a stream; null is bound as an SQL NULL.
     * @param data the data, may be null
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addBlob(final byte[] data) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, data, "blob"));
        if (data == null) {
            ps.setNull(i, Types.BLOB);
        } else {
            ps.setBinaryStream(i, new ByteArrayInputStream(data));
        }
        return this;
    }

    /**
     * Binds binary data to the next parameter, using {@code setBytes} if the database dialect requires it, e.g. for LONG RAW columns.
     * @param data the data
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement addBlobWithLongRawWorkaround(final byte[] data) throws SQLException {
        if (dialect.isLegacySetBytesRequired()) {
            final int i = ++col;
            args.add(new Arg(col, data, "blob-workaround/" + ps));
            ps.setBytes(i, data);
        } else {
            addBlob(data);
        }
        return this;
    }

    /**
     * Binds an SQL NULL of type INTEGER to the next parameter.
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("-> this")
    public SimplePreparedStatement addNullInteger() throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, null, "int"));
        ps.setNull(i, JDBCType.INTEGER.getVendorTypeNumber());
        return this;
    }

    /**
     * Binds an SQL NULL of type NUMERIC to the next parameter.
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("-> this")
    public SimplePreparedStatement addNullNumber() throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, null, "number"));
        ps.setNull(i, JDBCType.NUMERIC.getVendorTypeNumber());
        return this;
    }

    /**
     * Binds an SQL NULL of type VARCHAR to the next parameter.
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("-> this")
    public SimplePreparedStatement addNullString()  throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, null, "string"));
        ps.setNull(i, JDBCType.VARCHAR.getVendorTypeNumber());
        return this;
    }

    /**
     * @return the parameters that were set so far
     */
    @NotNull
    @Unmodifiable
    public List<Arg> getArgs() {
        return List.copyOf(args);
    }

    /**
     * Binds a long to the next parameter.
     * @param v the value
     * @return this statement
     * @throws SQLException if the value cannot be set
     */
    @NotNull
    @Contract("_ -> this")
    public SimplePreparedStatement add(final long v) throws SQLException {
        return addLong(v);
    }

    @NotNull
    @Override
    public String toString() {
        return "SimplePreparedStatement{" + "statement=" + statement + ", args=" + args + '}';
    }

    /** A parameter that was set on a prepared statement, kept for diagnostics. */
    public static class Arg {
        private final int col;
        private final Object value;
        private final String type;

        public Arg(final int col, final Object value, final String type) {
            this.col = col;
            this.value = value;
            this.type = type;
        }

        /**
         * @return the index of the parameter
         */
        public int getCol() {
            return col;
        }

        /**
         * @return the value of the parameter
         */
        public Object getValue() {
            return value;
        }

        /**
         * @return a description of the type of the parameter
         */
        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Arg{" + "col=" + col + ", value=" + value + ", type='" + type + '\'' + '}';
        }
    }

}
