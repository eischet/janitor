package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;

import java.util.List;

public class Script extends Statement {

    private final String source;
    private final List<Statement> statements;

    public Script(final Location location, final List<Statement> statements, final String source) {
        super(location);
        this.statements = statements;
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public List<Statement> getStatements() {
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
