package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.statement.Statement;
import org.eclipse.collections.api.list.ImmutableList;

public class Block extends Statement {

    final ImmutableList<Statement> statements;

    public Block(final Location location, final ImmutableList<Statement> statements) {
        super(location);
        this.statements = statements;
    }

    public ImmutableList<Statement> getStatements() {
        return statements;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            runningScript.setCurrentLocation(getLocation());
            if (getLocation() != null) {
                runningScript.enterBlock(null);
            }
            runningScript.trace(() -> "executing " + getStatements().size() + " statements...");
            for (final Statement statement : getStatements()) {
                runningScript.trace(() -> "executing: " + statement);
                statement.execute(runningScript);
            }
        } finally {
            runningScript.exitBlock();
        }
    }
}
