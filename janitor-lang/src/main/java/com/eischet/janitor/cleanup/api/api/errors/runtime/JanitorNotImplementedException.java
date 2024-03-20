package com.eischet.janitor.cleanup.api.api.errors.runtime;

import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

public class JanitorNotImplementedException extends JanitorRuntimeException {
    public JanitorNotImplementedException(final JanitorScriptProcess runningScript, final String s) {
        super(runningScript, s, JanitorNotImplementedException.class);
    }
}
