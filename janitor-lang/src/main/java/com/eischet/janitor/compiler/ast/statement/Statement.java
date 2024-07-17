package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.AstNode;

/**
 * Base class for all kinds of statements.
 */
public abstract class Statement extends AstNode {
    /**
     * Constructor.
     * @param location where
     */
    public Statement(final Location location) {
        super(location);
    }

    /**
     * Execute this statement.
     * @param runningScript the script process
     * @throws JanitorRuntimeException if something goes wrong
     * @throws JanitorControlFlowException on control flow events, which are currently implemented as exceptions
     */
    public abstract void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException;




}
