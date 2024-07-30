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
 * @see JanitorScriptProcess for the "running" part of a script.
 */
public interface RunnableScript {

    /**
     * Run the script with a given global scope.
     * @param prepareGlobals a consumer that can add variables to the global scope
     * @return the script's result
     * @throws JanitorRuntimeException on runtime errors
     */
    @NotNull JanitorObject run(@NotNull Consumer<Scope> prepareGlobals) throws JanitorRuntimeException;

    /**
     * Run the script with a given global scope, and keep the global scope afterward.
     * @param prepareGlobals a consumer that can add variables to the global scope
     * @return the script's result and the global scope
     * @throws JanitorRuntimeException on runtime errors
     */
    @NotNull ResultAndScope runAndKeepGlobals(@NotNull Consumer<Scope> prepareGlobals) throws JanitorRuntimeException;

    /**
     * Run the script with a given global scope.
     * The usual way of obtaining such a scope is by using runAndKeepGlobals.
     *
     * @param prepareGlobals a consumer that can add variables to the global scope
     * @param parentScope the parent scope to use
     * @return the script's result
     * @throws JanitorRuntimeException on runtime errors
     */
    @NotNull JanitorObject runInScope(@NotNull Consumer<Scope> prepareGlobals, Scope parentScope) throws JanitorRuntimeException;

    /**
     * Run the script with an empty global scope.
     * This is just shorthand for calling {@link #run(Consumer)} with an empty consumer.
     * @return the script's result
     * @throws JanitorRuntimeException on runtime errors
     */
    @NotNull default JanitorObject run() throws JanitorRuntimeException {
        return run(scope -> { });
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
     * @throws JanitorRuntimeException
     */
    @NotNull ResultAndScope runInScopeAndKeepGlobals(Scope parentScope) throws JanitorRuntimeException;
}
