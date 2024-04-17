package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

public class JanitorArgumentException extends JanitorRuntimeException {
    public JanitorArgumentException(final JanitorScriptProcess runningScript, final String formatted) {
        super(runningScript, formatted, JanitorArgumentException.class);
    }

    public JanitorArgumentException(final JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorArgumentException.class);
    }
}
