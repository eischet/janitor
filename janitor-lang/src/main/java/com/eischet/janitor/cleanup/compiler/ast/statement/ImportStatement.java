package com.eischet.janitor.cleanup.compiler.ast.statement;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.statement.controlflow.JanitorControlFlowException;
import org.eclipse.collections.api.list.ImmutableList;

public class ImportStatement extends Statement {
    private final ImmutableList<ImportClause> clauses;


    // private final QualifiedName qualifiedName;
    // private final String alias;

    public ImportStatement(final Location location, final ImmutableList<ImportClause> clauses /* final QualifiedName qualifiedName, final String alias */)  {
        super(location);
        this.clauses = clauses;
        // this.qualifiedName = qualifiedName;
        // this.alias = alias;
    }


    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        runningScript.setCurrentLocation(getLocation());
        for (final ImportClause clause : clauses) {
            clause.execute(runningScript);
        }
    }
}
