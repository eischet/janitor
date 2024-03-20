package com.eischet.janitor.cleanup.api.api.errors.runtime;

import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.statement.controlflow.JanitorControlFlowException;

public class JanitorInternalException extends JanitorRuntimeException {
    public JanitorInternalException(final JanitorScriptProcess runningScript, final String s, final JanitorControlFlowException e) {
        super(runningScript, s, e, JanitorInternalException.class);
    }
}
