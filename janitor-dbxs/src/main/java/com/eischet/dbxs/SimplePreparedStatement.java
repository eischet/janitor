/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.statements.GenericStatement;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

    public int getLatestColumnIndex() {
        return col;
    }

    public GenericStatement getStatement() {
        return statement;
    }

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

    public SimplePreparedStatement addIntFrom(final @NotNull ValueSource<Integer> source) throws SQLException {
        return addInt(source.getValue());
    }

    public SimplePreparedStatement addNull() throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, "NULL", "null"));
        ps.setNull(i, Types.NULL);
        return this;
    }

    public SimplePreparedStatement addDouble(final double v) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, v, "double"));
        ps.setDouble(i, v);
        return this;
    }

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

    public SimplePreparedStatement addDoubleInstanceFrom(final @NotNull ValueSource<Double> source) throws SQLException {
        return addDoubleInstance(source.getValue());
    }




    public SimplePreparedStatement addInts(final Integer... values) throws SQLException {
        for (final Integer value : values) {
            addInt(value);
        }
        return this;
    }

    public SimplePreparedStatement addString(final String v) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, v, "string"));
        ps.setString(i, v);
        return this;
    }

    public SimplePreparedStatement addStringFrom(final @NotNull ValueSource<String> source) throws SQLException {
        return addString(source.getValue());
    }


    public SimplePreparedStatement addStrings(final String... values) throws SQLException {
        for (final String value : values) {
            addString(value);
        }
        return this;
    }

    public SimplePreparedStatement addLong(final long v) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, v, "long"));
        ps.setLong(i, v);
        return this;
    }

    public SimplePreparedStatement addLongFrom(final @NotNull ValueSource<Long> source) throws SQLException {
        return addLong(source.getValue());
    }


    @SuppressWarnings("UnusedReturnValue")
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

    public SimplePreparedStatement addDate(final LocalDate date) throws SQLException {
        return addTimestamp(date.atStartOfDay());
    }

    public SimplePreparedStatement addTimestamp(final LocalDate timestamp) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, timestamp, "timestamp(ldt)"));
        ps.setTimestamp(i, timestamp(timestamp));
        return this;
    }

    public SimplePreparedStatement addTimestamp(final LocalDateTime timestamp) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, timestamp, "timestamp(ldt)"));
        ps.setTimestamp(i, timestamp(timestamp));
        return this;
    }

    public SimplePreparedStatement addLocalDateTime(final LocalDateTime timestamp) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, timestamp, "timestamp(ldt)"));
        ps.setTimestamp(i, timestamp(timestamp));
        return this;
    }

    public SimplePreparedStatement addLocalDateTimeFrom(final @NotNull ValueSource<LocalDateTime> source) throws SQLException {
        return addLocalDateTime(source.getValue());
    }

    private Timestamp timestamp(final LocalDate dt) {
        return dt == null ? null : Timestamp.valueOf(dt.atStartOfDay());
    }

    private Timestamp timestamp(final LocalDateTime dt) {
        return dt == null ? null : Timestamp.valueOf(dt);
    }

    public SimplePreparedStatement addTimestamp(final Timestamp timestamp) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, timestamp, "timestamp"));
        ps.setTimestamp(i, timestamp);
        return this;
    }

    public SimplePreparedStatement addNationalClob(final String clob) throws SQLException {
        // LATER: NCLOB muss wahrscheinlich anders behandelt werden als clob!
        return addClob(new StringReader(clob == null ? "" : clob));
    }

    public SimplePreparedStatement addClob(final String clob) throws SQLException {
        return addClob(new StringReader(clob == null ? "" : clob));
    }

    public SimplePreparedStatement addClob(final StringReader clob) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, clob, "clob"));
        dialect.addClobToStatement(ps, i, clob);
        return this;
    }

    public SimplePreparedStatement addBytes(final byte[] data) throws SQLException {
        // LATER: nur für sqlite!
        final int i = ++col;
        args.add(new Arg(col, data, "blob"));
        if (data == null || data.length == 0) {
            ps.setNull(i, Types.BLOB);
        } else {
            ps.setBytes(i, data);
        }
        return this;
    }

    public SimplePreparedStatement addBlob(final byte[] data) throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, data, "blob"));
        if (data == null || data.length == 0) {
            ps.setNull(i, Types.BLOB);
        } else {
            ps.setBinaryStream(i, new ByteArrayInputStream(data));
        }
        return this;
    }

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

    public SimplePreparedStatement addNullInteger() throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, null, "int"));
        ps.setNull(i, JDBCType.INTEGER.getVendorTypeNumber());
        return this;
    }

    public SimplePreparedStatement addNullNumber() throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, null, "number"));
        ps.setNull(i, JDBCType.NUMERIC.getVendorTypeNumber());
        return this;
    }

    public SimplePreparedStatement addNullString()  throws SQLException {
        final int i = ++col;
        args.add(new Arg(col, null, "string"));
        ps.setNull(i, JDBCType.VARCHAR.getVendorTypeNumber());
        return this;
    }

    public List<Arg> getArgs() {
        return args;
    }

    public SimplePreparedStatement add(final long v) throws SQLException {
        return addLong(v);
    }

    @Override
    public String toString() {
        return "SimplePreparedStatement{" + "statement=" + statement + ", args=" + args + '}';
    }

    public static class Arg {
        private final int col;
        private final Object value;
        private final String type;

        public Arg(final int col, final Object value, final String type) {
            this.col = col;
            this.value = value;
            this.type = type;
        }

        public int getCol() {
            return col;
        }

        public Object getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Arg{" + "col=" + col + ", value=" + value + ", type='" + type + '\'' + '}';
        }
    }

}
