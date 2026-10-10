/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.dialects;

import com.eischet.dbxs.metadata.SqlTypeInterpreter;
import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** The dialect for PostgreSQL. */
public class DatabaseDialectPostgres extends DatabaseDialectCommon {

    /** {@code LIMIT ? OFFSET ?} is supported by every version we run against. */
    @Override
    public boolean canLimitAndOffset(final DatabaseVersion databaseVersion) {
        return true;
    }

    @Override
    public @NotNull SelectStatement addLimitAndOffset(final @NotNull SelectStatement selectStatement) {
        return appendLimitThenOffset(selectStatement);
    }

    @Override
    public @NotNull SimplePreparedStatement addLimitAndOffset(final @NotNull SimplePreparedStatement statement, final int limit, final int offset) throws SQLException {
        return bindLimitThenOffset(statement, limit, offset);
    }

    @Override
    public SelectStatement getNextValueQuery(final @Nullable String schema, final @NotNull String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select nextval('" + seq + "')");
        } else {
            return new SelectStatement("select nextval('" + schema + "." + seq + "')");
        }
    }

    @Override
    public void addClobToStatement(final @NotNull PreparedStatement ps, final int i, final StringReader clob) throws SQLException {
        ps.setCharacterStream(i, clob); // necessary special handling for PostgreSQL, since setClob is apparently not implemented
    }

    @Override
    public @Nullable String readNationalClob(final @NotNull ResultSet rs, final int col) throws SQLException {
        try {
            final Reader reader = rs.getCharacterStream(col);
            if (reader == null) {
                return null;
            }
            return SqlTypeInterpreter.transferToString(reader);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public @Nullable String readRegularClob(final @NotNull ResultSet rs, final int col) throws SQLException {
        try {
            final Reader reader = rs.getCharacterStream(col);
            if (reader == null) {
                return null;
            }
            return SqlTypeInterpreter.transferToString(reader);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

}
