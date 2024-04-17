package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;

import java.util.List;

public class ImportStatement extends Statement {
    private final List<ImportClause> clauses;

    public ImportStatement(final Location location, final List<ImportClause> clauses) {
        super(location);
        this.clauses = clauses;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        runningScript.setCurrentLocation(getLocation());
        for (final ImportClause clause : clauses) {
            clause.execute(runningScript);
        }
    }
}
