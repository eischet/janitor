/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.dialects;

import com.eischet.dbxs.statements.SelectStatement;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseDialectPostgres extends DatabaseDialectCommon {

    @Override
    public SelectStatement getNextValueQuery(final String schema, final String seq) {
        if (schema == null || schema.isEmpty()) {
            return new SelectStatement("select nextval('" + seq + "')");
        } else {
            return new SelectStatement("select nextval('" + schema + "." + seq + "')");
        }
    }

    @Override
    public void addClobToStatement(final PreparedStatement ps, final int i, final StringReader clob) throws SQLException {
        ps.setCharacterStream(i, clob); // notwendige Sonderbehandlung für PostgreSQL, da setClob offenbar nicht implementiert wurde
    }

    @Override
    public String readNationalClob(final ResultSet rs, final int col) throws SQLException {
        try {
            final Reader reader = rs.getCharacterStream(col);
            final StringWriter writer = new StringWriter();
            reader.transferTo(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new SQLException(e);
        }

    }

    @Override
    public String readRegularClob(final ResultSet rs, final int col) throws SQLException {
        try {
            final Reader reader = rs.getCharacterStream(col);
            final StringWriter writer = new StringWriter();
            reader.transferTo(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

}
