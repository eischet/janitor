package com.eischet.dbxs.metadata;

import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for SqlTypes.fromJdbc(): it used to return null for an unmapped JDBC type code
 * instead of SqlTypes.UNKNOWN (unlike the very similar SqlTypeInterpreter.forSqlTypeCode(), which
 * already defaulted to UNKNOWN). SimpleResultSet's constructor stores fromJdbc()'s result directly
 * into a list that typeOf() -- annotated @NotNull -- reads from, so a null here could make typeOf()
 * violate its own contract for an exotic/vendor-specific column type.
 */
public class SqlTypesTestCase extends JanitorTest {

    @Test
    public void fromJdbcReturnsUnknownForAnUnmappedCode() {
        assertEquals(SqlTypes.UNKNOWN, SqlTypes.fromJdbc(Integer.MIN_VALUE + 12345));
    }

    @Test
    public void fromJdbcReturnsTheMatchingType() {
        assertEquals(SqlTypes.INTEGER, SqlTypes.fromJdbc(SqlTypes.INTEGER.getJdbcValue()));
    }

}
