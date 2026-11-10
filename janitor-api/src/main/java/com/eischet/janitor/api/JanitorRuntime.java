// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.VariableScope;
import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallable;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The runtime in which Janitor scripts run. This is a sub-object of an enviroment, and more short-lived.
 */
public interface JanitorRuntime {

    /**
     * @return the environment that this runtime belongs to
     */
    JanitorEnvironment getEnvironment();

    /**
     * Compiles a script.
     * @param moduleName the name of the script, for error messages
     * @param source the source code
     * @return the compiled script
     * @throws JanitorCompilerException if the source code cannot be compiled
     */
    RunnableScript compile(String moduleName, @Language("Janitor") String source) throws JanitorCompilerException;
    /**
     * Compiles a script to check it for errors, without running it.
     * @param moduleName the name of the script, for error messages
     * @param source the source code
     * @return the compiled script
     * @throws JanitorCompilerException if the source code cannot be compiled
     */
    RunnableScript checkCompile(String moduleName, @Language("Janitor") String source) throws JanitorCompilerException;
    /**
     * Implements the {@code print} function of scripts, whose output goes wherever the host wants it to.
     * @param process the running script process
     * @param args the values to print
     * @return the result of the call, usually null
     */
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
    default JanitorObject executeCallback(VariableScope scope, JCallable callable, List<JanitorObject> args) throws JanitorRuntimeException {
        return executeCallback(scope, callable, args, process -> { });
    }

    /**
     * Run a callback function in a new process, which the setup callback can configure before the function runs.
     * The new process does not inherit anything from the script that the callback came from; to keep its meta-data,
     * call {@link JanitorScriptProcess#inheritMetaData(JanitorScriptProcess)} in the setup.
     *
     * @param scope a suitable scope that contains e.g. globals used by the callable
     * @param callable the callable
     * @param args arguments to be passed to the callable
     * @param setup receives the process before the function runs
     * @return whatever the callable chooses to return
     */
    JanitorObject executeCallback(VariableScope scope, JCallable callable, List<JanitorObject> args, @NotNull Consumer<JanitorScriptProcess> setup) throws JanitorRuntimeException;


    /**
     * Emits a trace message, if tracing is enabled.
     * @param traceMessageSupplier supplies the message; it is only called if the message is needed
     */
    void trace(Supplier<String> traceMessageSupplier);


    /**
     * @return the built-in types of the environment
     */
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
