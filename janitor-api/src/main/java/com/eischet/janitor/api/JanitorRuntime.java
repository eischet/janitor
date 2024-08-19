package com.eischet.janitor.api;

import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.types.JanitorObject;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * The runtime in which Janitor scripts run. This is a sub-object of an enviroment, and more short-lived.
 */
public interface JanitorRuntime {

    JanitorEnvironment getEnvironment();

    RunnableScript compile(String moduleName, @Language("Janitor") String source) throws JanitorCompilerException;
    RunnableScript checkCompile(String moduleName, @Language("Janitor") String source) throws JanitorCompilerException;
    JanitorObject print(JanitorScriptProcess process, JCallArgs args);


    void trace(Supplier<String> traceMessageSupplier);

    default JanitorCompilerSettings getCompilerSettings() {
        return JanitorCompilerSettings.DEFAUlTS;
    }

    default BuiltinTypes getBuiltinTypes() {
        return getEnvironment().getBuiltinTypes();
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

    /**
     * Run script code without throwing a script runtime exception on errors.
     * The environment may report an exception, but it may not throw.
     * @param title a name for the protected code block, shown in an exception report
     * @param call the code to execute
     */
    void protect(final @NotNull String title, @NotNull JanitorScriptProcess.ProtectedCall call);
}
