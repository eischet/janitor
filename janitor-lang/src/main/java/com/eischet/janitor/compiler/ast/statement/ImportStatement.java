package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;

import java.util.List;

/**
 * Import statement.
 * This statement consists of at least one import clause, e.g. import foo, bar, baz;.
 *
 * @see ImportClause for a helper class that does all the work
 */
public class ImportStatement extends Statement {
    private final List<ImportClause> clauses;

    /**
     * Constructor.
     * @param location where
     * @param clauses what to import
     */
    public ImportStatement(final Location location, final List<ImportClause> clauses) {
        super(location);
        this.clauses = clauses;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        process.setCurrentLocation(getLocation());
        for (final ImportClause clause : clauses) {
            clause.execute(process);
        }
    }
}
