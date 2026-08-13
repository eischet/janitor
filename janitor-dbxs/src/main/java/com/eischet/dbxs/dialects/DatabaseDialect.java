/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.dialects;

import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.dbxs.metadata.SqlTypeInterpreter;
import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseDialect {

    default @NotNull String quoteColumn(@NotNull String columnName) {
        return columnName;
    }

    default @NotNull String quoteTableName(@NotNull String tableName) {
        return quoteTableName(null, tableName);
    }

    default @NotNull String quoteTableName(@Nullable String schema, @NotNull String tableName) {
        if (schema == null || schema.isEmpty()) {
            return tableName;
        } else {
            return schema + "." + tableName;
        }
    }

    boolean limitAndOffsetRequiresOrderBy();
    boolean canLimitAndOffset(final DatabaseVersion databaseVersion);

    @NotNull SelectStatement addLimitAndOffset(@NotNull SelectStatement selectStatement);
    @NotNull SimplePreparedStatement addLimitAndOffset(@NotNull SimplePreparedStatement statement, int limit, int offset) throws SQLException;

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
    SelectStatement getNextValueQuery(@Nullable String schema, @NotNull String seq);

    @Nullable
    default SelectStatement getNextValueQuery(@NotNull String sequence) {
        return getNextValueQuery(null, sequence);
    }

    @Nullable
    SelectStatement getCurrentValueQuery(@Nullable String schema, @NotNull String seq);

    default void addClobToStatement(@NotNull PreparedStatement ps, int i, StringReader clob) throws SQLException {
        ps.setClob(i, clob);
    }

    default @Nullable String readNationalClob(@NotNull ResultSet rs, int col) throws SQLException {
        final Clob clob = rs.getClob(col);
        if (clob == null) {
            return null;
        }
        try (final @Nullable Reader reader = clob.getCharacterStream()) {
            if (reader == null) {
                return null;
            }
            return SqlTypeInterpreter.transferToString(reader);
        } catch (IOException e) {
            throw new SQLException(e);
        }

    }

    default @Nullable String readRegularClob(@NotNull ResultSet rs, int col) throws SQLException {
        final Clob clob = rs.getClob(col);
        if (clob == null) {
            return null;
        }
        try (final @Nullable Reader reader = clob.getCharacterStream()) {
            if (reader == null) {
                return null;
            }
            return SqlTypeInterpreter.transferToString(reader);
        } catch (IOException e) {
            throw new SQLException(e);
        }

    }

    default boolean isLegacySetBytesRequired() {
        return false;
    }
}
