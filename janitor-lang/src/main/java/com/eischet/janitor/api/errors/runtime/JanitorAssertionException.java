package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.types.JanitorScriptProcess;

public class JanitorAssertionException extends JanitorRuntimeException {
    public JanitorAssertionException(final JanitorScriptProcess process) {
        super(process, JanitorAssertionException.class);
    }

    public JanitorAssertionException(final JanitorScriptProcess process, final String message) {
        super(process, message, JanitorAssertionException.class);
    }

    public JanitorAssertionException(final JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorAssertionException.class);
    }

    public JanitorAssertionException(final JanitorScriptProcess process, final Throwable cause) {
        super(process, cause, JanitorAssertionException.class);
    }

}
