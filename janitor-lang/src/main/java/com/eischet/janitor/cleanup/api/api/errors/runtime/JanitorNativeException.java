package com.eischet.janitor.cleanup.api.api.errors.runtime;

import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

public class JanitorNativeException extends JanitorRuntimeException {
    public JanitorNativeException(final JanitorScriptProcess rs, final String s, final Exception e) {
        super(rs, s, e, JanitorNativeException.class);
    }
}
