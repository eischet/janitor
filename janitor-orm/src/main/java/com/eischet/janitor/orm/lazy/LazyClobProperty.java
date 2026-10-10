// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.lazy;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.results.SimpleResultSet;

import java.sql.SQLException;

/** A lazily loaded long text (CLOB) property. */
public class LazyClobProperty extends LazyProperty<String> {
    public LazyClobProperty(final String propertyName) {
        super(propertyName);
    }

    @Override
    public String readFromResultSet(final SimpleResultSet resultSet) throws SQLException {
        return resultSet.readClob();
    }
}
