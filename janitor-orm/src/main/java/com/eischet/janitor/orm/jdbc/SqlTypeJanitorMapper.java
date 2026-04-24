package com.eischet.janitor.orm.jdbc;

import com.eischet.dbxs.metadata.SqlTypes;
import com.eischet.dbxs.results.SimpleResultSet;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Mapper for converting JDBC SQL types to Janitor objects.
 */
public class SqlTypeJanitorMapper {

    /**
     * Read a single value from the Result Set.
     * @param type the SQL type of the value
     * @param rs the result set to read from
     * @param i the column index
     * @return the value
     * @throws SQLException on errors
     */
    public static JanitorObject read(SqlTypes type, final SimpleResultSet rs, final int i) throws SQLException {
        return switch (type) {
            case NULL -> Janitor.NULL;
            case BIT, INTEGER, TINYINT, SMALLINT -> Janitor.nullableInteger(rs.getLongInstance(i));
            case BIGINT -> {
                final BigDecimal bigDecimal = rs.getRealResultSet().getBigDecimal(i);
                if (bigDecimal == null) {
                    yield Janitor.NULL;
                }
                try {
                    yield Janitor.integer(bigDecimal.longValueExact());
                } catch (ArithmeticException e) {
                    yield Janitor.numeric(bigDecimal.doubleValue());
                }
            }
            case FLOAT, REAL, DOUBLE, NUMERIC, DECIMAL -> Janitor.nullableNumeric(rs.getDoubleInstance(i));
            case BOOLEAN -> Janitor.toBool(rs.getRealResultSet().getBoolean(i));
            case DATE, TIME, TIME_WITH_TIMEZONE, TIMESTAMP_WITH_TIMEZONE, TIMESTAMP -> Janitor.nullableDateTime(rs.getLocalDateTime(i));
            case BINARY, VARBINARY, LONGVARBINARY, BLOB -> Janitor.binary(rs.readBlob(i));
            case LONGVARCHAR, CLOB -> Janitor.nullableString(rs.readClob(i));
            case LONGNVARCHAR, NCLOB -> Janitor.nullableString(rs.readNationalClob(i));
            case CHAR, VARCHAR, NCHAR, NVARCHAR, SQLXML, REF_CURSOR, UNKNOWN, OTHER, JAVA_OBJECT, DISTINCT, STRUCT, ARRAY, REF, DATALINK, ROWID ->
                    Janitor.nullableString(rs.getString(i));
        };
    }

    /**
     * Read one row from the Result Set and return it as a list of objects.
     * @param rs the result set to read from
     * @return a list of objects
     * @throws SQLException when the JDBC driver throws it
     */
    public static JList readForScript(final SimpleResultSet rs) throws SQLException {
        final int numberOfColumns = rs.getNumberOfColumns();
        final JList list = Janitor.list(numberOfColumns);
        for (int i = 0; i < numberOfColumns; i++) {
            list.add(read(rs.typeOf(i+1), rs, i+1));
        }
        return list;
    }

}
