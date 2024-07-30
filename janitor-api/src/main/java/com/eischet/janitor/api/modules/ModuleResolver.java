package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ModuleResolver {
    @Nullable
    JanitorModule resolveModuleByStringName(JanitorScriptProcess process, String name) throws JanitorRuntimeException;
}
