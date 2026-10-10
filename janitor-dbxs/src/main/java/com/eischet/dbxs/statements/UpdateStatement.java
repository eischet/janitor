/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

/** An SQL statement that changes data, e.g. an insert, update or delete. */
public class UpdateStatement extends GenericStatement {
    public UpdateStatement(@NotNull @Language("SQL") final String sql) {
        super(sql);
    }
    /**
     * @param sql the SQL text
     * @return a new update statement
     */
    public static @NotNull UpdateStatement of(@NotNull @Language("SQL") final String sql) {
        return new UpdateStatement(sql);
    }
}
