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

public class DatabaseDialectOracle extends DatabaseDialectCommon {

    @Override
    public boolean canLimitAndOffset(final DatabaseVersion databaseVersion) {
        return databaseVersion.getMajorVersion() >= 12;
    }

    @Override
    public @NotNull SelectStatement addLimitAndOffset(final @NotNull SelectStatement selectStatement) {
        return new SelectStatement(selectStatement.getSql() + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    }

    // LATER: es wird zwei Varianten geben müssen: eine, die ZUERST limit/offset setzt, und diese hier die es am Ende tut. Glaube ich.

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
        return true; // applies to LONG RAW, which is sadly still used in a few assyst databases...
    }
}
