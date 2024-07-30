package com.eischet.janitor.api;

import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * The runtime in which Janitor scripts run. This is a sub-object of an enviroment, and more short-lived.
 * TODO: this is probably reduntant with JanitorEnvironment, and should be merged into it, though I'm not yet sure.
 */
public interface JanitorRuntime {

    JanitorEnvironment getEnvironment();

    RunnableScript compile(String moduleName, String source) throws JanitorCompilerException;
    RunnableScript checkCompile(String moduleName, String source) throws JanitorCompilerException;


    void registerModule(final JanitorModuleRegistration moduleRegistration);
    @NotNull JanitorModule getModuleByQualifier(final JanitorScriptProcess process, String name) throws JanitorRuntimeException;

    @NotNull JanitorModule getModuleByStringName(final JanitorScriptProcess process, String name) throws JanitorRuntimeException;

    JanitorObject print(JanitorScriptProcess rs, JCallArgs args);


    void trace(Supplier<String> traceMessageSupplier);

    default JanitorCompilerSettings getCompilerSettings() {
        return JanitorCompilerSettings.DEFAUlTS;
    }


    /**
     * Log a warning message.
     * How (and if) this message is displayed is up to a runtime or environment implementation.
     * The default implementation just forwards to the environment.
     * @param warning a warning message
     */
    default void warn(String warning) {
        getEnvironment().warn(warning);
    }

    @NotNull JanitorFormatting getFormatting();

    void protect(final String title, JanitorScriptProcess.ProtectedCall call);
}
