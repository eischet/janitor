package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.AstNode;

public abstract class Statement extends AstNode {
    public Statement(final Location location) {
        super(location);
    }

    public abstract void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException;




}
