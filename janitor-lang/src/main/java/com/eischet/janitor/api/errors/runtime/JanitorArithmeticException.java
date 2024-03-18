package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.types.JanitorScriptProcess;

public class JanitorArithmeticException extends JanitorRuntimeException {
    public JanitorArithmeticException(final JanitorScriptProcess process, final String s, final ArithmeticException e) {
        super(process, s, e, JanitorArithmeticException.class);
    }
}
