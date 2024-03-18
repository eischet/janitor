package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.types.JanitorScriptProcess;

public class JanitorNotImplementedException extends JanitorRuntimeException {
    public JanitorNotImplementedException(final JanitorScriptProcess runningScript, final String s) {
        super(runningScript, s, JanitorNotImplementedException.class);
    }
}
