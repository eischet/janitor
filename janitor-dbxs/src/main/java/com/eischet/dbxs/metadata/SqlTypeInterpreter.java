/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.metadata;

import com.eischet.janitor.toolbox.memory.Interner;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum SqlTypeInterpreter {

    // the codes come from java.sql.Types!
    UNKNOWN("UNKNOWN", Integer.MIN_VALUE, SqlTypeInterpreter::readStringAndIntern), // used as a default
    BIT("BIT", Types.BIT, ResultSet::getBoolean),
    TINYINT("TINYINT", Types.TINYINT, SqlTypeInterpreter::readIntegerAndIntern),
    SMALLINT("SMALLINT", Types.SMALLINT, SqlTypeInterpreter::readIntegerAndIntern),
    INTEGER("INTEGER", Types.INTEGER, SqlTypeInterpreter::readIntegerAndIntern),
    BIGINT("BIGINT", Types.BIGINT, (SimpleExtractor) ResultSet::getBigDecimal),
    FLOAT("FLOAT", Types.FLOAT, ResultSet::getFloat),
    REAL("REAL", Types.REAL, ResultSet::getDouble),
    DOUBLE("DOUBLE", Types.DOUBLE, ResultSet::getDouble),
    // LATER Fix: NUMERIC("NUMERIC", Types.NUMERIC, ResultSet::getDouble),
    DECIMAL("DECIMAL", Types.DECIMAL, (SimpleExtractor) ResultSet::getBigDecimal),
    CHAR("CHAR", Types.CHAR, SqlTypeInterpreter::readStringAndIntern),
    VARCHAR("VARCHAR", Types.VARCHAR, SqlTypeInterpreter::readStringAndIntern),
    LONGVARCHAR("LONGVARCHAR", Types.LONGVARCHAR, SqlTypeInterpreter::readAsTextStream),
    DATE("DATE", Types.DATE, (SimpleExtractor) ResultSet::getTimestamp),
    TIME("TIME", Types.TIME, (SimpleExtractor) ResultSet::getTime),
    TIMESTAMP("TIMESTAMP", Types.TIMESTAMP, (SimpleExtractor) ResultSet::getTimestamp),
    BINARY("BINARY", Types.BINARY, SqlTypeInterpreter::readAsBinaryStream),
    VARBINARY("VARBINARY", Types.VARBINARY, SqlTypeInterpreter::readAsBinaryStream),
    LONGVARBINARY("LONGVARBINARY", Types.LONGVARBINARY, SqlTypeInterpreter::readAsBinaryStream),
    NULL("NULL", Types.NULL, (rs, i) -> null),
    OTHER("OTHER", Types.OTHER, SqlTypeInterpreter::readStringAndIntern),
    JAVA_OBJECT("JAVA_OBJECT", Types.JAVA_OBJECT, (SimpleExtractor) ResultSet::getObject),
    DISTINCT("DISTINCT", Types.DISTINCT, SqlTypeInterpreter::readStringAndIntern),
    STRUCT("STRUCT", Types.STRUCT, SqlTypeInterpreter::readStringAndIntern),
    ARRAY("ARRAY", Types.ARRAY, ResultSet::getArray),
    BLOB("BLOB", Types.BLOB, SqlTypeInterpreter::readAsBinaryStream),
    CLOB("CLOB", Types.CLOB, SqlTypeInterpreter::readAsTextStream),
    REF("REF", Types.REF, ResultSet::getRef),
    DATALINK("DATALINK", Types.DATALINK, SqlTypeInterpreter::readStringAndIntern),
    BOOLEAN("BOOLEAN", Types.BOOLEAN, ResultSet::getBoolean),
    ROWID("ROWID", Types.ROWID, ResultSet::getRowId),
    NCHAR("NCHAR", Types.NCHAR, SqlTypeInterpreter::readStringAndIntern),
    NVARCHAR("NVARCHAR", Types.NVARCHAR, SqlTypeInterpreter::readStringAndIntern),
    LONGNVARCHAR("LONGVARCHAR", Types.LONGNVARCHAR, SqlTypeInterpreter::readAsTextStream),
    NCLOB("NCLOB", Types.NCLOB, SqlTypeInterpreter::readAsTextStream),
    SQLXML("SQLXML", Types.SQLXML, ResultSet::getSQLXML),
    REF_CURSOR("REF_CURSOR", Types.REF_CURSOR, ResultSet::getRef),
    TIME_WITH_TIMEZONE("TIME_WITH_TIMEZONE", Types.TIME_WITH_TIMEZONE, (SimpleExtractor) ResultSet::getTimestamp),
    TIMESTAMP_WITH_TIMEZONE("TIMESTAMP_WITH_TIMEZONE", Types.TIMESTAMP_WITH_TIMEZONE, (SimpleExtractor) ResultSet::getTimestamp),
    NUMERIC("NUMERIC", Types.NUMERIC, (rs, i, nr) -> {
        // beim Default gilt: INCIDENT_ID gut, Balken schlecht... daher kann man das jetzt übersteuern im Statement.
        if (nr) {
            return rs.getDouble(i);
        } else {
            return readIntegerAndIntern(rs, i);
        }
    });

    public static String readStringAndIntern(final ResultSet rs, final int index) throws SQLException {
        return Interner.maybeIntern(rs.getString(index));
    }

    private final String typeName;
    private final int typeCode;
    private final Extractor extractor;

    SqlTypeInterpreter(final String typeName, final int typeCode, final SimpleExtractor extractor) {
        this.typeName = typeName;
        this.typeCode = typeCode;
        this.extractor = wrap(extractor);
    }

    public static SqlTypeInterpreter byTypeName(final String n) {
        return Arrays.stream(values()).filter(it -> Objects.equals(it.typeName, n)).findFirst().orElse(null);
    }

    public Extractor getExtractor() {
        return extractor;
    }

    SqlTypeInterpreter(final String typeName, final int typeCode, final Extractor extractor) {
        this.typeName = typeName;
        this.typeCode = typeCode;
        this.extractor = extractor;
    }

    public static Integer readIntegerAndIntern(final ResultSet rs, final int index) throws SQLException {
        return Interner.maybeIntern(rs.getInt(index));
    }

    public static byte[] readAsBinaryStream(final ResultSet rs, final int index) throws SQLException {
        final InputStream stream = rs.getBinaryStream(index);
        if (stream == null) {
            return null;
        } else {
            try {
                return toByteArray(stream);
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }
    }

    public static byte[] toByteArray(final InputStream stream) throws IOException {
        if (stream == null) {
            return new byte[0];
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stream.transferTo(baos);
        return baos.toByteArray();
    }

    public static String transferToString(final Reader stream) throws IOException {
        final StringWriter out = new StringWriter();
        stream.transferTo(out);
        return out.toString();
    }


    public static String readAsTextStream(final ResultSet rs, final int index) throws SQLException {
        final Reader stream = rs.getCharacterStream(index);
        if (stream == null) {
            return "";
        } else {
            try {
                return Interner.maybeIntern(transferToString(stream));
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }
    }



    public String getTypeName() {
        return toString();
    }


    public interface Extractor {
        Object extract(final ResultSet rs, final int index, final boolean numReal) throws SQLException;
    }

    public interface SimpleExtractor {
        Object extract(final ResultSet rs, final int index) throws SQLException;
    }

    private Extractor wrap(final SimpleExtractor wrappee) {
        return (rs, index, numReal) -> wrappee.extract(rs, index);
    }

    private static final Map<Integer, SqlTypeInterpreter> interpreters = new HashMap<>();
    private static final Map<String, SqlTypeInterpreter> namedInterpreters = new HashMap<>();

    static {
        for (final SqlTypeInterpreter interpreter : values()) {
            interpreters.put(interpreter.typeCode, interpreter);
            namedInterpreters.put(interpreter.typeName, interpreter);
        }
    }

    public static Map<String, SqlTypeInterpreter> getNamedInterpreters() {
        return namedInterpreters;
    }

    public static SqlTypeInterpreter forSqlTypeCode(int code) {
        return interpreters.getOrDefault(code, UNKNOWN);
    }

}
