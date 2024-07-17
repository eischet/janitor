package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.statement.Statement;

import java.util.List;

/**
 * Block of statements.
 * That's really just a simple list of other statements, dressed as a statement.
 */
public class Block extends Statement {

    private final List<Statement> statements;

    /**
     * Constructor.
     *
     * @param location   where
     * @param statements the statements
     */
    public Block(final Location location, final List<Statement> statements) {
        super(location);
        this.statements = statements;
    }

    /**
     * Get the statements.
     *
     * @return the statements
     */
    public List<Statement> getStatements() {
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
