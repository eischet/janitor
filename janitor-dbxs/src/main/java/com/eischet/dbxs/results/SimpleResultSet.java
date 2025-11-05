/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.results;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.metadata.SqlTypeInterpreter;
import com.eischet.dbxs.metadata.SqlTypes;
import com.eischet.janitor.toolbox.memory.Interner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(SimpleResultSet.class);

    private final DatabaseDialect dialect;
    private final ResultSet rs;

    private final ResultSetMetaData metaData;
    private final DatabaseConnection connection;
    private final List<SqlTypes> types;
    private final int numberOfColumns;

    private int rowNumber = -1;
    private int colNumber = 1;

    public SimpleResultSet(final DatabaseDialect dialect, final ResultSet rs, final DatabaseConnection connection) throws SQLException {
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

    public DatabaseConnection getConnection() {
        return connection;
    }

    public SqlTypes typeOf(int column) {
        if (column > types.size()) {
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

    public ResultSet getRealResultSet() {
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

    public String getString(final int columnIndex) throws SQLException {
        return SqlTypeInterpreter.readStringAndIntern(rs, columnIndex);
    }

    public String getString() throws SQLException {
        return getString(colNumber++);
    }

    @NotNull
    public Integer getInt(final int columnIndex) throws SQLException {
        return SqlTypeInterpreter.readIntegerAndIntern(rs, columnIndex);
    }

    @NotNull
    public Integer getInt() throws SQLException {
        return getInt(colNumber++);
    }

    public long getLong(final int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }

    public long getLong() throws SQLException {
        return getLong(colNumber++);
    }

    public Long getLongInstance(final int columnIndex) throws SQLException {
        final long mappedValue = rs.getLong(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return mappedValue; // return SqlTypeInterpreter.maybeIntern(mappedValue);
        }
    }

    public Long getLongInstance() throws SQLException {
        return getLongInstance(colNumber++);
    }

    public float getFloat(final int columnIndex) throws SQLException {
        return rs.getFloat(columnIndex);
    }

    public float getFloat() throws SQLException {
        return getFloat(colNumber++);
    }

    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return rs.getTimestamp(columnIndex);
    }

    public Timestamp getTimestamp() throws SQLException {
        return getTimestamp(colNumber++);
    }

    public Clob getClob(final int columnIndex) throws SQLException {
        return rs.getClob(columnIndex);
    }

    public Clob getClob() throws SQLException {
        return getClob(colNumber++);
    }

    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return rs.getBinaryStream(columnIndex);
    }

    public InputStream getBinaryStream() throws SQLException {
        return getBinaryStream(colNumber++);
    }

    public double getDouble(final int columnIndex) throws SQLException {
        return rs.getDouble(columnIndex);
    }

    public double getDouble() throws SQLException {
        return getDouble(colNumber++);
    }

    public Date getDate(final int columnIndex) throws SQLException {
        return rs.getDate(columnIndex);
    }

    public Date getDate() throws SQLException {
        return getDate(colNumber++);
    }

    public LocalDateTime getLocalDateTime(final int columnIndex) throws SQLException {
        return date(rs.getTimestamp(columnIndex));
    }

    public LocalDateTime getLocalDateTime() throws SQLException {
        return getLocalDateTime(colNumber++);
    }

    public LocalDate getLocalDate() throws SQLException {
        final LocalDateTime ldt = getLocalDateTime();
        if (ldt == null) {
            return null;
        }
        return ldt.toLocalDate();
    }

    public boolean isFirstRow() {
        return rowNumber == 0;
    }

    public Integer getInteger(final int col) throws SQLException {
        final int mappedValue = rs.getInt(col);
        if (rs.wasNull()) {
            return null;
        } else {
            return Interner.maybeIntern(mappedValue);
        }
    }

    public Integer getInteger() throws SQLException {
        return getInteger(colNumber++);
    }

    public Double getDoubleInstance(final int col) throws SQLException {
        final double mappedValue = rs.getDouble(col);
        if (rs.wasNull()) {
            return null;
        } else {
            return mappedValue;
        }
    }

    public Double getDoubleInstance() throws SQLException {
        return getDoubleInstance(colNumber++);
    }


    public byte[] readBlob(final int col) throws SQLException {
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

    public byte[] readBlob() throws SQLException {
        return readBlob(colNumber++);
    }

    public String readNationalClob(final int col) throws SQLException {
        return dialect.readNationalClob(rs, col);
    }

    public String readClob(final int col) throws SQLException {
        return dialect.readRegularClob(rs, col);
    }

    public String readNationalClob() throws SQLException {
        return readNationalClob(colNumber++);
    }

    public String readClob() throws SQLException {
        return readClob(colNumber++);
    }

    public Timestamp getOptionalTimestamp(final int i) throws SQLException {
        if (numberOfColumns >= i) {
            return getTimestamp(i);
        } else {
            return null;
        }
    }


    protected LocalDateTime date(final Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }


}


