/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;

import org.intellij.lang.annotations.Language;

public class UpdateStatement extends GenericStatement {
    public UpdateStatement(@Language("SQL") final String sql) {
        super(sql);
    }
    public static UpdateStatement of(@Language("SQL") final String sql) {
        return new UpdateStatement(sql);
    }
}
