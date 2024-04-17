package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

public class JanitorInternalException extends JanitorRuntimeException {
    public JanitorInternalException(final JanitorScriptProcess runningScript, final String s, final JanitorControlFlowException e) {
        super(runningScript, s, e, JanitorInternalException.class);
    }
}
