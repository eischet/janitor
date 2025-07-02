package com.eischet.janitor.api;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallable;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * The runtime in which Janitor scripts run. This is a sub-object of an enviroment, and more short-lived.
 */
public interface JanitorRuntime {

    JanitorEnvironment getEnvironment();

    RunnableScript compile(String moduleName, @Language("Janitor") String source) throws JanitorCompilerException;
    RunnableScript checkCompile(String moduleName, @Language("Janitor") String source) throws JanitorCompilerException;
    JanitorObject print(JanitorScriptProcess process, JCallArgs args);

    /**
     * Run a callback function.
     * Use case: you obtain a callback from a script and later call this, e.g. to filter or manipulate a list of values.
     *
     * @param scope a suitable scope that contains e.g. globals used by the callable
     * @param callable the callable
     * @param args arguments to be passed to the callable
     * @return whatever the callable chooses to return
     */
    JanitorObject executeCallback(Scope scope, JCallable callable, List<JanitorObject> args) throws JanitorRuntimeException;


    void trace(Supplier<String> traceMessageSupplier);


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
