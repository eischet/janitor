package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.statement.controlflow.JanitorControlFlowException;
import org.eclipse.collections.api.list.ImmutableList;

public class Script extends Statement {

    private final String source;
    private final ImmutableList<Statement> statements;

    public Script(final Location location, final ImmutableList<Statement> statements, final String source) {
        super(location);
        this.statements = statements;
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public ImmutableList<Statement> getStatements() {
        return statements;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        runningScript.trace(() -> "executing " + getStatements().size() + " statements at top level...");
        for (final Statement statement : getStatements()) {
            runningScript.trace(() -> "executing: " + statement);
            statement.execute(runningScript);
        }
    }
}
