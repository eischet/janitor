package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

/** Creates a {@link JanitorModule} on demand, e.g. when a script imports it for the first time. */
@FunctionalInterface
public interface JanitorModuleSupplier {
    /**
     * Creates the module.
     * @param process the running script process
     * @return the module
     * @throws JanitorRuntimeException if the module cannot be created
     */
    JanitorModule getModule(final JanitorScriptProcess process) throws JanitorRuntimeException;
}
