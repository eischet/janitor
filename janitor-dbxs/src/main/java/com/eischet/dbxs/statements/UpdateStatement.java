/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public class UpdateStatement extends GenericStatement {
    public UpdateStatement(@NotNull @Language("SQL") final String sql) {
        super(sql);
    }
    public static @NotNull UpdateStatement of(@NotNull @Language("SQL") final String sql) {
        return new UpdateStatement(sql);
    }
}
