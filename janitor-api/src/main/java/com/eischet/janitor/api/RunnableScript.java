// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * A script that is ready to run.
 * Unless it was compiled in test mode and produces issues, then it's likely not runnable and can be queried for issues.
 * <p>
 * The run methods take a callback that receives the {@link JanitorScriptProcess} after it has been created and before the
 * script starts. It binds the script's global variables, e.g. {@code script.run(p -> p.bind("order", order))}, and can
 * configure the process, e.g. {@code p.setMaxInstructionCount(10_000)}, which overrides the environment's default.
 * </p>
 * @see JanitorScriptProcess for the "running" part of a script.
 */
public interface RunnableScript {

    /**
     * Runs the script.
     * @param setup receives the process before the script starts, to bind global variables and configure the process
     * @return the script's result
     * @throws JanitorRuntimeException on runtime errors
     */
    @NotNull JanitorObject run(@NotNull Consumer<JanitorScriptProcess> setup) throws JanitorRuntimeException;

    /**
     * Runs the script, and keeps its global scope afterward.
     * @param setup receives the process before the script starts, to bind global variables and configure the process
     * @return the script's result and the global scope
     * @throws JanitorRuntimeException on runtime errors
     */
    @NotNull ResultAndScope runAndKeepGlobals(@NotNull Consumer<JanitorScriptProcess> setup) throws JanitorRuntimeException;

    /**
     * Run the script with an empty global scope, and keep the global scope afterward.
     * This is probably less useful than {@link #runAndKeepGlobals(Consumer)}, but might be usable for e.g. config files.
     * @return the script's result and the global scope
     * @throws JanitorRuntimeException on runtime errors
     */
    default @NotNull ResultAndScope runAndKeepGlobals() throws JanitorRuntimeException {
        return runAndKeepGlobals(process -> { });
    }

    /**
     * Runs the script in a new scope below the given one.
     * The usual way of obtaining such a scope is by using runAndKeepGlobals.
     *
     * @param setup receives the process before the script starts, to bind global variables and configure the process
     * @param parentScope the parent scope to use
     * @return the script's result
     * @throws JanitorRuntimeException on runtime errors
     */
    @NotNull JanitorObject runInScope(@NotNull Consumer<JanitorScriptProcess> setup, Scope parentScope) throws JanitorRuntimeException;

    /**
     * Run the script with an empty global scope.
     * This is just shorthand for calling {@link #run(Consumer)} with an empty callback.
     * @return the script's result
     * @throws JanitorRuntimeException on runtime errors
     */
    @NotNull default JanitorObject run() throws JanitorRuntimeException {
        return run(process -> { });
    }

    /**
     * Get a list of issues encountered during compilation.
     * This should usually be empty,
     * @return compilation errors and warnings
     */
    @NotNull List<String> getIssues();

    /**
     * Get a compiler exception, if any.
     * @return a compiler exception, usually null
     */
    @Nullable Exception getCompilerException();

    /**
     * Experimental method for JSR223, because that needs a way of retrieving stuff from the original scope.
     * @param parentScope  the scope to run in
     * @return result and scope
     * @throws JanitorRuntimeException on errors
     */
    @NotNull ResultAndScope runInScopeAndKeepGlobals(Scope parentScope) throws JanitorRuntimeException;
}
