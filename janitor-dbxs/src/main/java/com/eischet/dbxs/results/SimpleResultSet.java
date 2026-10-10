// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs.results;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.metadata.SqlTypeInterpreter;
import com.eischet.dbxs.metadata.SqlTypes;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.toolbox.memory.Interner;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A more natural way of working with result set objects.
 * There is, intentionally, no wasNull method. Use Number-Instance getters instead, like getInteger(), which handle wasNull
 * internally for you.
 */
public class SimpleResultSet {

    private static final JanitorLogger log = JanitorLogger.getLogger(SimpleResultSet.class);

    private final DatabaseDialect dialect;
    private final ResultSet rs;

    private final ResultSetMetaData metaData;
    private final DatabaseConnection connection;
    private final List<SqlTypes> types;
    private final int numberOfColumns;

    private int rowNumber = -1;
    private int colNumber = 1;

    /**
     * Wraps a raw JDBC ResultSet. This is called by DatabaseConnection implementations (e.g.
     * SimpleDataManager) as part of running a query -- client code should not construct this
     * directly; get one via {@link DatabaseConnection#queryForEach} / {@code queryForObject} /
     * {@code queryForList} etc. instead, which is the only way a real query actually produces one.
     */
    @ApiStatus.Internal
    public SimpleResultSet(@NotNull final DatabaseDialect dialect,
                           @NotNull final ResultSet rs,
                           @NotNull final DatabaseConnection connection) throws SQLException {
        this.dialect = dialect;
        this.rs = rs;
        this.metaData = rs.getMetaData();
        this.connection = connection;
        int _numberOfColumns;
        try {
            _numberOfColumns = metaData.getColumnCount();
        } catch (SQLException err) {
            log.warn("error getting column count for result set via {}", dialect, err);
            _numberOfColumns = -1;
        }
        final List<SqlTypes> types = new ArrayList<>();
        if (_numberOfColumns >= 0) {
            for (int i = 0; i < _numberOfColumns; i++) {
                try {
                    types.add(SqlTypes.fromJdbc(metaData.getColumnType(i + 1)));
                } catch (SQLException ignored) {
                    types.add(SqlTypes.UNKNOWN);
                }
            }
        }
        this.numberOfColumns = _numberOfColumns;
        this.types = types;
    }

    /**
     * @return the database connection that the result set belongs to
     */
    public @NotNull DatabaseConnection getConnection() {
        return connection;
    }

    /**
     * @param column the index of the column, starting at 1
     * @return the type of the column, or UNKNOWN if there is no such column
     */
    public @NotNull SqlTypes typeOf(int column) {
        if (column < 1 || column > types.size()) {
            return SqlTypes.UNKNOWN;
        } else {
            return types.get(column-1);
        }
    }

    /**
     * @return the number of columns in the result set
     */
    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    /**
     * @return the number of the current row, starting at 0 for the first row
     */
    public int getRowNumber() {
        return rowNumber;
    }

    /**
     * @return the underlying JDBC result set
     */
    public @NotNull ResultSet getRealResultSet() {
        return rs;
    }

    /**
     * @return the meta-data of the result set
     */
    public ResultSetMetaData getMetaData() {
        return metaData;
    }

    /**
     * Moves to the next row.
     * @return true if there is a next row
     * @throws SQLException on database errors
     */
    public boolean next() throws SQLException {
        ++rowNumber;
        colNumber = 1;
        return rs.next();
    }

    /**
     * Closes the result set.
     * @throws SQLException on database errors
     */
    public void close() throws SQLException {
        rs.close();
    }

    /**
     * Reads a string from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the string, interned if it is short, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable String getString(final int columnIndex) throws SQLException {
        return SqlTypeInterpreter.readStringAndIntern(rs, columnIndex);
    }

    /**
     * Reads a string from the next column after the one that was read last.
     * @return the string, interned if it is short, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable String getString() throws SQLException {
        return getString(colNumber++);
    }

    /**
     * Reads a int value from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the value, or 0 if the column is NULL
     * @throws SQLException on database errors
     */
    public int getInt(final int columnIndex) throws SQLException {
        return rs.getInt(columnIndex);
    }

    /**
     * Reads a int value from the next column after the one that was read last.
     * @return the value, or 0 if the column is NULL
     * @throws SQLException on database errors
     */
    public int getInt() throws SQLException {
        return getInt(colNumber++);
    }

    /**
     * Reads a long value from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the value, or 0 if the column is NULL
     * @throws SQLException on database errors
     */
    public long getLong(final int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }

    /**
     * Reads a long value from the next column after the one that was read last.
     * @return the value, or 0 if the column is NULL
     * @throws SQLException on database errors
     */
    public long getLong() throws SQLException {
        return getLong(colNumber++);
    }

    /**
     * Reads a nullable long from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the value, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Long getLongInstance(final int columnIndex) throws SQLException {
        final long mappedValue = rs.getLong(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return mappedValue; // return SqlTypeInterpreter.maybeIntern(mappedValue);
        }
    }

    /**
     * Reads a nullable long from the next column after the one that was read last.
     * @return the value, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Long getLongInstance() throws SQLException {
        return getLongInstance(colNumber++);
    }

    /**
     * Reads a float value from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the value, or 0 if the column is NULL
     * @throws SQLException on database errors
     */
    public float getFloat(final int columnIndex) throws SQLException {
        return rs.getFloat(columnIndex);
    }

    /**
     * Reads a float value from the next column after the one that was read last.
     * @return the value, or 0 if the column is NULL
     * @throws SQLException on database errors
     */
    public float getFloat() throws SQLException {
        return getFloat(colNumber++);
    }

    /**
     * Reads a timestamp from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the timestamp, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return rs.getTimestamp(columnIndex);
    }

    /**
     * Reads a timestamp from the next column after the one that was read last.
     * @return the timestamp, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Timestamp getTimestamp() throws SQLException {
        return getTimestamp(colNumber++);
    }

    /**
     * Reads a CLOB from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the CLOB, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Clob getClob(final int columnIndex) throws SQLException {
        return rs.getClob(columnIndex);
    }

    /**
     * Reads a CLOB from the next column after the one that was read last.
     * @return the CLOB, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Clob getClob() throws SQLException {
        return getClob(colNumber++);
    }

    /**
     * Reads a binary stream from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the stream, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return rs.getBinaryStream(columnIndex);
    }

    /**
     * Reads a binary stream from the next column after the one that was read last.
     * @return the stream, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable InputStream getBinaryStream() throws SQLException {
        return getBinaryStream(colNumber++);
    }

    /**
     * Reads a double value from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the value, or 0 if the column is NULL
     * @throws SQLException on database errors
     */
    public double getDouble(final int columnIndex) throws SQLException {
        return rs.getDouble(columnIndex);
    }

    /**
     * Reads a double value from the next column after the one that was read last.
     * @return the value, or 0 if the column is NULL
     * @throws SQLException on database errors
     */
    public double getDouble() throws SQLException {
        return getDouble(colNumber++);
    }

    /**
     * Reads a date from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the date, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Date getDate(final int columnIndex) throws SQLException {
        return rs.getDate(columnIndex);
    }

    /**
     * Reads a date from the next column after the one that was read last.
     * @return the date, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Date getDate() throws SQLException {
        return getDate(colNumber++);
    }

    /**
     * Reads a local datetime from the given column.
     * @param columnIndex the index of the column, starting at 1
     * @return the datetime, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable LocalDateTime getLocalDateTime(final int columnIndex) throws SQLException {
        return date(rs.getTimestamp(columnIndex));
    }

    /**
     * Reads a local datetime from the next column after the one that was read last.
     * @return the datetime, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable LocalDateTime getLocalDateTime() throws SQLException {
        return getLocalDateTime(colNumber++);
    }

    /**
     * Reads a local date from the next column after the one that was read last.
     * @return the date, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable LocalDate getLocalDate() throws SQLException {
        final LocalDateTime ldt = getLocalDateTime();
        if (ldt == null) {
            return null;
        }
        return ldt.toLocalDate();
    }

    /**
     * @return true if the result set is positioned at its first row
     */
    public boolean isFirstRow() {
        return rowNumber == 0;
    }

    /**
     * Reads a nullable integer from the given column.
     * @param col the index of the column, starting at 1
     * @return the value, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Integer getIntegerInstance(final int col) throws SQLException {
        final int mappedValue = rs.getInt(col);
        if (rs.wasNull()) {
            return null;
        } else {
            return Interner.maybeIntern(mappedValue);
        }
    }

    /**
     * Reads a nullable integer from the next column after the one that was read last.
     * @return the value, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Integer getIntegerInstance() throws SQLException {
        return getIntegerInstance(colNumber++);
    }

    /**
     * Reads a nullable double from the given column.
     * @param col the index of the column, starting at 1
     * @return the value, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Double getDoubleInstance(final int col) throws SQLException {
        final double mappedValue = rs.getDouble(col);
        if (rs.wasNull()) {
            return null;
        } else {
            return mappedValue;
        }
    }

    /**
     * Reads a nullable double from the next column after the one that was read last.
     * @return the value, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Double getDoubleInstance() throws SQLException {
        return getDoubleInstance(colNumber++);
    }

    /**
     * Reads binary data (BLOB) from the given column.
     * @param col the index of the column, starting at 1
     * @return the data, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public byte @Nullable [] readBlob(final int col) throws SQLException {
        final InputStream stream = rs.getBinaryStream(col);
        if (rs.wasNull() || stream == null) {
            return null;
        }
        try {
            return SqlTypeInterpreter.toByteArray(stream);
        } catch (IOException e) {
            throw new SQLException("error reading BLOB", e);
        }
    }

    /**
     * Reads binary data (BLOB) from the next column after the one that was read last.
     * @return the data, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public byte @Nullable [] readBlob() throws SQLException {
        return readBlob(colNumber++);
    }

    /**
     * Reads a long text in a national character set (NCLOB) from the given column.
     * @param col the index of the column, starting at 1
     * @return the text, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable String readNationalClob(final int col) throws SQLException {
        return dialect.readNationalClob(rs, col);
    }

    /**
     * Reads a long text (CLOB) from the given column.
     * @param col the index of the column, starting at 1
     * @return the text, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable String readClob(final int col) throws SQLException {
        return dialect.readRegularClob(rs, col);
    }

    /**
     * Reads a long text in a national character set (NCLOB) from the next column after the one that was read last.
     * @return the text, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable String readNationalClob() throws SQLException {
        return readNationalClob(colNumber++);
    }

    /**
     * Reads a long text (CLOB) from the next column after the one that was read last.
     * @return the text, or null if the column is NULL
     * @throws SQLException on database errors
     */
    public @Nullable String readClob() throws SQLException {
        return readClob(colNumber++);
    }

    /**
     * Reads a timestamp from a column, if the result set has that many columns.
     * @param i the index of the column, starting at 1
     * @return the timestamp, or null if the column does not exist or is NULL
     * @throws SQLException on database errors
     */
    public @Nullable Timestamp getOptionalTimestamp(final int i) throws SQLException {
        if (numberOfColumns >= i) {
            return getTimestamp(i);
        } else {
            return null;
        }
    }


    /**
     * @param timestamp the timestamp, may be null
     * @return the local datetime, or null if the timestamp is null
     */
    protected @Nullable LocalDateTime date(final @Nullable Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }


}


