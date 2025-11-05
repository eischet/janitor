/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.statements;

import org.intellij.lang.annotations.Language;

public class SelectStatement extends GenericStatement {
    public SelectStatement(@Language("SQL") final String sql) {
        super(sql);
    }
    public static SelectStatement of(@Language("SQL") final String sql) {
        return new SelectStatement(sql);
    }
}
