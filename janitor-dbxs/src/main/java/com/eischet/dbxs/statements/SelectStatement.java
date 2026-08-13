/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public class SelectStatement extends GenericStatement {
    public SelectStatement(@NotNull @Language("SQL") final String sql) {
        super(sql);
    }
    public static @NotNull SelectStatement of(@NotNull @Language("SQL") final String sql) {
        return new SelectStatement(sql);
    }
}
