package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;

import java.util.List;

/**
 * The root of all scripting.
 * This is the top-level script, containing all statements.
 */
public class Script extends Statement {

    private final String source;
    private final List<Statement> statements;

    /**
     * Constructor.
     *
     * @param location   where
     * @param statements list of statements
     * @param source     source code
     */
    public Script(final Location location, final List<Statement> statements, final String source) {
        super(location);
        this.statements = statements;
        this.source = source;
    }

    /**
     * Get the source code.
     *
     * @return source code
     */
    public String getSource() {
        return source;
    }

    /**
     * Get the list of statements.
     *
     * @return list of statements
     */
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
