package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

public class JanitorScriptModuleException extends JanitorRuntimeException {
    public JanitorScriptModuleException(@NotNull JanitorScriptProcess process) {
        super(process, JanitorScriptModuleException.class);
    }

    public JanitorScriptModuleException(@NotNull JanitorScriptProcess process, String message) {
        super(process, message, JanitorScriptModuleException.class);
    }

    public JanitorScriptModuleException(@NotNull JanitorScriptProcess process, String message, Throwable cause) {
        super(process, message, cause, JanitorScriptModuleException.class);
    }

    public JanitorScriptModuleException(@NotNull JanitorScriptProcess process, Throwable cause) {
        super(process, cause, JanitorScriptModuleException.class);
    }
}
