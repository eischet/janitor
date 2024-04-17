package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

public class JanitorNativeException extends JanitorRuntimeException {
    public JanitorNativeException(final JanitorScriptProcess rs, final String s, final Exception e) {
        super(rs, s, e, JanitorNativeException.class);
    }
}
