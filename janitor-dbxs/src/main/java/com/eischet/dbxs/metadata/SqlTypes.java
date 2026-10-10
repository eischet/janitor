package com.eischet.dbxs.metadata;

import org.jetbrains.annotations.NotNull;

import java.sql.Types;
import java.util.Arrays;

/** The SQL column types that are known to this library, mapped to the constants of {@link java.sql.Types}. */
public enum SqlTypes {

    BIT(Types.BIT),
    TINYINT(Types.TINYINT),
    SMALLINT(Types.SMALLINT),
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    FLOAT(Types.FLOAT),
    REAL(Types.REAL),
    DOUBLE(Types.DOUBLE),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    CHAR(Types.CHAR),
    VARCHAR(Types.VARCHAR),
    LONGVARCHAR(Types.LONGVARCHAR),
    DATE(Types.DATE),
    TIME(Types.TIME),
    TIMESTAMP(Types.TIMESTAMP),
    BINARY(Types.BINARY),
    VARBINARY(Types.VARBINARY),
    LONGVARBINARY(Types.LONGVARBINARY),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    JAVA_OBJECT(Types.JAVA_OBJECT),
    DISTINCT(Types.DISTINCT),
    STRUCT(Types.STRUCT),
    ARRAY(Types.ARRAY),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    REF(Types.REF),
    DATALINK(Types.DATALINK),
    BOOLEAN(Types.BOOLEAN),
    ROWID(Types.ROWID),
    NCHAR(Types.NCHAR),
    NVARCHAR(Types.NVARCHAR),
    LONGNVARCHAR(Types.LONGNVARCHAR),
    NCLOB(Types.NCLOB),
    SQLXML(Types.SQLXML),
    REF_CURSOR(Types.REF_CURSOR),
    TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE),
    TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE),

    UNKNOWN(Integer.MAX_VALUE)
    ;

    private final int jdbcValue;

    SqlTypes(int jdbcValue) {
        this.jdbcValue = jdbcValue;
    }

    /**
     * @return the corresponding constant from {@link java.sql.Types}
     */
    public int getJdbcValue() {
        return jdbcValue;
    }

    /**
     * @param jdbcValue the type code from {@link java.sql.Types}
     * @return the corresponding type, or UNKNOWN if there is none
     */
    public static @NotNull SqlTypes fromJdbc(final int jdbcValue) {
        return Arrays.stream(values()).filter(v -> v.jdbcValue == jdbcValue).findFirst().orElse(UNKNOWN);
    }

}
