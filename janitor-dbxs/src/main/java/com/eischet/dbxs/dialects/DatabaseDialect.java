/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.dialects;

import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseDialect {

    default String quoteColumn(String columnName) {
        return columnName;
    }

    default String quoteTableName(String tableName) {
        return quoteTableName(null, tableName);
    }

    default String quoteTableName(String schema, String tableName) {
        if (schema == null || schema.isEmpty()) {
            return tableName;
        } else {
            return schema + "." + tableName;
        }
    }

    boolean limitAndOffsetRequiresOrderBy();
    boolean canLimitAndOffset(final DatabaseVersion databaseVersion);

    SelectStatement addLimitAndOffset(SelectStatement selectStatement);
    SimplePreparedStatement addLimitAndOffset(SimplePreparedStatement statement, int limit, int offset) throws SQLException;

    /**
     * Gibt ein Statement zurück, das den nächsten Wert aus der angegebenen Sequence holt.
     *
     * Nicht alle Datenbanken <b>haben</b> Sequences, aber das ist nicht schlimm, weil diese Funktion nur intern
     * genutzt wird und nicht für Kunden zugänglich ist. Darum kann sie nur gegen unterstützte Datenbanken
     * ausgeführt werden, das sind aktuell PostgreSQL, Oracle und MS SQL, und die haben allesamt Sequences.
     *
     * @param schema optionales Schema
     * @param seq Name der Sequence
     * @return eine Abfrage für den nächsten Wert der Sequence, oder null wenn die Datenbank keine Sequences kennt
     */
    @Nullable
    SelectStatement getNextValueQuery(String schema, String seq);

    @Nullable
    default SelectStatement getNextValueQuery(String sequence) {
        return getNextValueQuery(null, sequence);
    }

    @Nullable
    SelectStatement getCurrentValueQuery(String schema, String seq);

    default void addClobToStatement(PreparedStatement ps, int i, StringReader clob) throws SQLException {
        ps.setClob(i, clob);
    }

    default String readNationalClob(ResultSet rs, int col) throws SQLException {
        final Clob clob = rs.getClob(col);
        if (clob == null) {
            return null;
        }
        try (final Reader reader = clob.getCharacterStream()) {
            final StringWriter writer = new StringWriter();
            reader.transferTo(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new SQLException(e);
        }

    }

    default String readRegularClob(ResultSet rs, int col) throws SQLException {
        final Clob clob = rs.getClob(col);
        if (clob == null) {
            return null;
        }
        try (final Reader reader = clob.getCharacterStream()) {
            final StringWriter writer = new StringWriter();
            reader.transferTo(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new SQLException(e);
        }

    }

    default boolean isLegacySetBytesRequired() {
        return false;
    }
}
