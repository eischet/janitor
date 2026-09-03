/*
 * © Eischet Software e.K., Köln
 */

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

    public @NotNull DatabaseConnection getConnection() {
        return connection;
    }

    public @NotNull SqlTypes typeOf(int column) {
        if (column < 1 || column > types.size()) {
            return SqlTypes.UNKNOWN;
        } else {
            return types.get(column-1);
        }
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public @NotNull ResultSet getRealResultSet() {
        return rs;
    }

    public ResultSetMetaData getMetaData() {
        return metaData;
    }

    public boolean next() throws SQLException {
        ++rowNumber;
        colNumber = 1;
        return rs.next();
    }

    public void close() throws SQLException {
        rs.close();
    }

    public @Nullable String getString(final int columnIndex) throws SQLException {
        return SqlTypeInterpreter.readStringAndIntern(rs, columnIndex);
    }

    public @Nullable String getString() throws SQLException {
        return getString(colNumber++);
    }

    public int getInt(final int columnIndex) throws SQLException {
        return rs.getInt(columnIndex);
    }

    public int getInt() throws SQLException {
        return getInt(colNumber++);
    }

    public long getLong(final int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }

    public long getLong() throws SQLException {
        return getLong(colNumber++);
    }

    public @Nullable Long getLongInstance(final int columnIndex) throws SQLException {
        final long mappedValue = rs.getLong(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return mappedValue; // return SqlTypeInterpreter.maybeIntern(mappedValue);
        }
    }

    public @Nullable Long getLongInstance() throws SQLException {
        return getLongInstance(colNumber++);
    }

    public float getFloat(final int columnIndex) throws SQLException {
        return rs.getFloat(columnIndex);
    }

    public float getFloat() throws SQLException {
        return getFloat(colNumber++);
    }

    public @Nullable Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return rs.getTimestamp(columnIndex);
    }

    public @Nullable Timestamp getTimestamp() throws SQLException {
        return getTimestamp(colNumber++);
    }

    public @Nullable Clob getClob(final int columnIndex) throws SQLException {
        return rs.getClob(columnIndex);
    }

    public @Nullable Clob getClob() throws SQLException {
        return getClob(colNumber++);
    }

    public @Nullable InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return rs.getBinaryStream(columnIndex);
    }

    public @Nullable InputStream getBinaryStream() throws SQLException {
        return getBinaryStream(colNumber++);
    }

    public double getDouble(final int columnIndex) throws SQLException {
        return rs.getDouble(columnIndex);
    }

    public double getDouble() throws SQLException {
        return getDouble(colNumber++);
    }

    public @Nullable Date getDate(final int columnIndex) throws SQLException {
        return rs.getDate(columnIndex);
    }

    public @Nullable Date getDate() throws SQLException {
        return getDate(colNumber++);
    }

    public @Nullable LocalDateTime getLocalDateTime(final int columnIndex) throws SQLException {
        return date(rs.getTimestamp(columnIndex));
    }

    public @Nullable LocalDateTime getLocalDateTime() throws SQLException {
        return getLocalDateTime(colNumber++);
    }

    public @Nullable LocalDate getLocalDate() throws SQLException {
        final LocalDateTime ldt = getLocalDateTime();
        if (ldt == null) {
            return null;
        }
        return ldt.toLocalDate();
    }

    public boolean isFirstRow() {
        return rowNumber == 0;
    }

    public @Nullable Integer getIntegerInstance(final int col) throws SQLException {
        final int mappedValue = rs.getInt(col);
        if (rs.wasNull()) {
            return null;
        } else {
            return Interner.maybeIntern(mappedValue);
        }
    }

    public @Nullable Integer getIntegerInstance() throws SQLException {
        return getIntegerInstance(colNumber++);
    }

    public @Nullable Double getDoubleInstance(final int col) throws SQLException {
        final double mappedValue = rs.getDouble(col);
        if (rs.wasNull()) {
            return null;
        } else {
            return mappedValue;
        }
    }

    public @Nullable Double getDoubleInstance() throws SQLException {
        return getDoubleInstance(colNumber++);
    }

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

    public byte @Nullable [] readBlob() throws SQLException {
        return readBlob(colNumber++);
    }

    public @Nullable String readNationalClob(final int col) throws SQLException {
        return dialect.readNationalClob(rs, col);
    }

    public @Nullable String readClob(final int col) throws SQLException {
        return dialect.readRegularClob(rs, col);
    }

    public @Nullable String readNationalClob() throws SQLException {
        return readNationalClob(colNumber++);
    }

    public @Nullable String readClob() throws SQLException {
        return readClob(colNumber++);
    }

    public @Nullable Timestamp getOptionalTimestamp(final int i) throws SQLException {
        if (numberOfColumns >= i) {
            return getTimestamp(i);
        } else {
            return null;
        }
    }


    protected @Nullable LocalDateTime date(final @Nullable Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }


}


