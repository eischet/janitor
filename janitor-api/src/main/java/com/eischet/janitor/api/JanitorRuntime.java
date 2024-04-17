package com.eischet.janitor.api;

import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface JanitorRuntime extends JanitorEnvironment {

    JanitorEnvironment getEnvironment();

    RunnableScript compile(String moduleName, String source) throws JanitorCompilerException;
    RunnableScript checkCompile(String moduleName, String source) throws JanitorCompilerException;


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

    JanitorObject lookupClassAttribute(final JanitorScriptProcess runningScript, JanitorObject instance, String attributeName);
}
