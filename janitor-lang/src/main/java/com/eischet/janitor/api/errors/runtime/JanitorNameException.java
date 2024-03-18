package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.types.JanitorScriptProcess;

public class JanitorNameException extends JanitorRuntimeException {
    public JanitorNameException(final JanitorScriptProcess process) {
        super(process, JanitorNameException.class);
    }

    public JanitorNameException(final JanitorScriptProcess process, final String message) {
        super(process, message, JanitorNameException.class);
    }

    public JanitorNameException(final JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorNameException.class);
    }

    public JanitorNameException(final JanitorScriptProcess process, final Throwable cause) {
        super(process, cause, JanitorNameException.class);
    }

}
