package com.eischet.janitor.cleanup.api.api.errors.runtime;

import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

public class JanitorArithmeticException extends JanitorRuntimeException {
    public JanitorArithmeticException(final JanitorScriptProcess process, final String s, final ArithmeticException e) {
        super(process, s, e, JanitorArithmeticException.class);
    }
}
