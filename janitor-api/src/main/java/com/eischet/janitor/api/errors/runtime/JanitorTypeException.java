package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

public class JanitorTypeException extends JanitorRuntimeException {
    public JanitorTypeException(final JanitorScriptProcess process, final String formatted) {
        super(process, formatted, JanitorTypeException.class);
    }
}
