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
import java.io.StringWriter;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseDialectOracle extends DatabaseDialectCommon {

    @Override
    public boolean canLimitAndOffset(final DatabaseVersion databaseVersion) {
        return databaseVersion.getMajorVersion() >= 12;
    }


    @Override
    public SelectStatement addLimitAndOffset(final SelectStatement selectStatement) {
        return new SelectStatement(selectStatement.getSql() + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    }

    // LATER: es wird zwei Varianten geben müssen: eine, die ZUERST limit/offset setzt, und diese hier die es am Ende tut. Glaube ich.

    @Override
    public SimplePreparedStatement addLimitAndOffset(final SimplePreparedStatement statement, final int limit, final int offset) throws SQLException {
        return statement.addInt(offset).addInt(limit);
    }

    @Override
    public SelectStatement getNextValueQuery(final String schema, final String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select " + seq + ".nextval from dual");
        } else {
            return new SelectStatement("select " + schema + "." + seq + ".nextval from dual");
        }
    }

    @Override
    public @Nullable SelectStatement getCurrentValueQuery(final String schema, final String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select " + seq + ".currval from dual");
        } else {
            return new SelectStatement("select " + schema + "." + seq + ".currval from dual");
        }
    }

    @Override
    public String readNationalClob(final ResultSet rs, final int col) throws SQLException {
        final NClob nclob = rs.getNClob(col);
        if (nclob == null) {
            return null;
        }
        try (final Reader reader = nclob.getCharacterStream()) {
            final StringWriter writer = new StringWriter();
            reader.transferTo(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new SQLException(e);
        }

    }

    @Override
    public boolean isLegacySetBytesRequired() {
        return true; // applies to LONG RAW, which is sadly still used in a few assyst databases...
    }
}
