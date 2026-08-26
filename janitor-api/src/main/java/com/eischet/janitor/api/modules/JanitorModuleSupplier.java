package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

@FunctionalInterface
public interface JanitorModuleSupplier {
    JanitorModule getModule(final JanitorScriptProcess process) throws JanitorRuntimeException;
}
