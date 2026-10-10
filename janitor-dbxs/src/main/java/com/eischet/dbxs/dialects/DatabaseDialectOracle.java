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
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

/** The dialect for Oracle databases. */
public class DatabaseDialectOracle extends DatabaseDialectCommon {

    /**
     * Reserved words (V$RESERVED_WORDS, RESERVED = 'Y') that are plausible as column names. Oracle upper-cases
     * unquoted identifiers, so we quote these in upper case, which assumes that the schema was created without quoting.
     */
    private static final Set<String> RESERVED_WORDS = Set.of(
            "ACCESS", "AUDIT", "COMMENT", "CURRENT", "DATE", "DEFAULT", "DELETE", "DESC", "FILE", "GROUP", "INDEX",
            "INITIAL", "LEVEL", "LOCK", "MODE", "NUMBER", "OFFLINE", "ONLINE", "ORDER", "PRIOR", "RAW", "RESOURCE",
            "ROW", "ROWID", "ROWNUM", "ROWS", "SESSION", "SIZE", "START", "SYSDATE", "TABLE", "TIMESTAMP", "TRIGGER",
            "UID", "UNIQUE", "USER", "VALIDATE", "VALUES", "VIEW", "WHEN", "WHERE"
    );

    @Override
    public @NotNull String quoteColumn(final @NotNull String columnName) {
        final String upper = columnName.toUpperCase(Locale.ROOT);
        return RESERVED_WORDS.contains(upper) ? "\"" + upper + "\"" : columnName;
    }

    /**
     * Oracle treats the empty string as NULL, so "= ''" never matches.
     */
    @Override
    public @NotNull String isEmptyCondition(final @NotNull String quotedColumn) {
        return quotedColumn + " is null";
    }

    @Override
    public @NotNull String isNotEmptyCondition(final @NotNull String quotedColumn) {
        return quotedColumn + " is not null";
    }

    @Override
    public boolean canLimitAndOffset(final DatabaseVersion databaseVersion) {
        return databaseVersion.getMajorVersion() >= 12;
    }

    @Override
    public @NotNull SelectStatement addLimitAndOffset(final @NotNull SelectStatement selectStatement) {
        return new SelectStatement(selectStatement.getSql() + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    }

    // LATER: we will probably need two variants: one that applies limit/offset FIRST, and this one, which applies it at the end.

    @Override
    public @NotNull SimplePreparedStatement addLimitAndOffset(final @NotNull SimplePreparedStatement statement, final int limit, final int offset) throws SQLException {
        return statement.addInt(offset).addInt(limit);
    }

    @Override
    public @NotNull SelectStatement getNextValueQuery(final @Nullable String schema, final @NotNull String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select " + seq + ".nextval from dual");
        } else {
            return new SelectStatement("select " + schema + "." + seq + ".nextval from dual");
        }
    }

    @Override
    public @Nullable SelectStatement getCurrentValueQuery(final @Nullable String schema, final @NotNull String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select " + seq + ".currval from dual");
        } else {
            return new SelectStatement("select " + schema + "." + seq + ".currval from dual");
        }
    }

    @Override
    public @Nullable String readNationalClob(final @NotNull ResultSet rs, final int col) throws SQLException {
        final NClob nclob = rs.getNClob(col);
        if (nclob == null) {
            return null;
        }
        try (final Reader reader = nclob.getCharacterStream()) {
            if (reader == null) {
                return null;
            }
            return SqlTypeInterpreter.transferToString(reader);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public boolean isLegacySetBytesRequired() {
        return true; // applies to LONG RAW, which is sadly still found in some legacy databases
    }
}
