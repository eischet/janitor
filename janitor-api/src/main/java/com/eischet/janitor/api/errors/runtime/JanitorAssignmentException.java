package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

public class JanitorAssignmentException extends JanitorRuntimeException {

    public JanitorAssignmentException(final JanitorScriptProcess process) {
        super(process, JanitorAssignmentException.class);
    }

    public JanitorAssignmentException(final JanitorScriptProcess process, final String message) {
        super(process, message, JanitorAssignmentException.class);
    }

    public JanitorAssignmentException(final JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorAssignmentException.class);
    }

    public JanitorAssignmentException(final JanitorScriptProcess process, final Throwable cause) {
        super(process, cause, JanitorAssignmentException.class);
    }
}
