package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

public class JanitorInstructionLimitExceededException extends JanitorRuntimeException {
    public JanitorInstructionLimitExceededException(final @NotNull JanitorScriptProcess process, long limit) {
        super(process, "Script execution exceeded maximum instruction count: " + limit, JanitorInstructionLimitExceededException.class);
    }
}
