package com.eischet.janitor.cleanup.compiler.ast.statement;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.AstNode;
import com.eischet.janitor.cleanup.compiler.ast.statement.controlflow.JanitorControlFlowException;

public abstract class Statement extends AstNode {
    public Statement(final Location location) {
        super(location);
    }

    public abstract void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException;




}
