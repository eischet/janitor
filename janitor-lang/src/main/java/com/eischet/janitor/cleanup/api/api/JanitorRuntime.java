package com.eischet.janitor.cleanup.api.api;

import com.eischet.janitor.cleanup.api.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.types.JanitorModule;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.JanitorCompilerSettings;
import com.eischet.janitor.cleanup.runtime.JanitorFormatting;
import com.eischet.janitor.cleanup.runtime.JanitorScript;
import com.eischet.janitor.cleanup.runtime.scope.Scope;
import com.eischet.janitor.cleanup.runtime.types.JCallArgs;
import com.eischet.janitor.cleanup.runtime.types.JanitorModuleRegistration;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface JanitorRuntime {

    default JanitorScript compile(String moduleName, String source) throws JanitorCompilerException {
        try {
            return new JanitorScript(this, moduleName, source == null ? "" : source);
        } catch (RuntimeException e) {
            throw new JanitorCompilerException("Compiler Error", e);
        }
    }

    default JanitorScript checkCompile(String moduleName, String source) throws JanitorCompilerException {
        try {
            return new JanitorScript(this, moduleName, source == null ? "" : source, true);
        } catch (RuntimeException e) {
            throw new JanitorCompilerException("Compiler Error", e);
        }
    }


    void registerModule(final JanitorModuleRegistration moduleRegistration);
    @NotNull JanitorModule getModuleByQualifier(final JanitorScriptProcess process, String name) throws JanitorRuntimeException;

    @NotNull JanitorModule getModuleByStringName(final JanitorScriptProcess process, String name) throws JanitorRuntimeException;

    JanitorObject print(JanitorScriptProcess rs, JCallArgs args);


    void trace(Supplier<String> traceMessageSupplier);

    default void prepareGlobals(Scope globalScope) {
    }

    default JanitorCompilerSettings getCompilerSettings() {
        return JanitorCompilerSettings.DEFAUlTS;
    }



    void warn(String warning);

    @NotNull JanitorFormatting getFormatting();
}
