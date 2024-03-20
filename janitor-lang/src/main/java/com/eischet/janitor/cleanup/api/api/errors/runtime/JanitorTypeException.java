package com.eischet.janitor.cleanup.api.api.errors.runtime;

import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

public class JanitorTypeException extends JanitorRuntimeException {
    public JanitorTypeException(final JanitorScriptProcess process, final String formatted) {
        super(process, formatted, JanitorTypeException.class);
    }
}
