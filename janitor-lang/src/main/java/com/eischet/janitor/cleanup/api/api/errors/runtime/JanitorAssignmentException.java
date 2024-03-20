package com.eischet.janitor.cleanup.api.api.errors.runtime;

import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

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
