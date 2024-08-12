package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ModuleResolver {
    /**
     * Find a module by name.
     * This code is to be implemented by embedders, there's no built-in implementation for it.
     *
     * @param process the running script process
     * @param name the name of the module
     * @return the module if found, or null if not found
     * @throws JanitorRuntimeException on errors
     */
    @Nullable
    JanitorModule resolveModuleByStringName(JanitorScriptProcess process, String name) throws JanitorRuntimeException;
}
