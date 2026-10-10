// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.types.JanitorCleanupRequired;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.functions.JCallArgs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Represents a running script.
 * This is used mainly by the interpreter to actually run the script, but also by host code that needs to interact with a script
 * or do things for the script which require access to the interpreter's internals. In the latter context, this interface can
 * be seen as a "handle" to the interpreter.
 */
public interface JanitorScriptProcess {

    /**
     * Emit a warning.
     * <p>
     * This is meant to be implemented by client code. Typically, this will log a warning level message.
     * </p>
     *
     * @param warning the warning
     */
    void warn(final String warning);

    /**
     * Returns a name for the process.
     *
     * @return a name for the process
     */
    @NotNull String getProcessName();

    /**
     * Retrieve the source code, if available, of the currently running script's main module.
     *
     * @return source code
     */
    @Nullable String getSource();

    /**
     * @return the scope of the main script
     */
    Scope getMainScope();

    @NotNull Scope getCurrentScope();

    /**
     * @return the runtime that this process runs in
     */
    JanitorRuntime getRuntime();

    /**
     * Enters a block of code, which gets its own scope.
     * @param location the location of the block, or null for anonymous blocks
     */
    void enterBlock(final Location location);

    /** Leaves the block that was entered last. */
    void exitBlock();

    /**
     * Finds the scope that defines a variable.
     * @param id the name of the variable
     * @return the variable's value together with the scope that it was found in
     */
    ResultAndScope lookupScopedVar(String id);

    /**
     * @return the location in the source code that is currently executed
     */
    Location getCurrentLocation();

    /**
     * Sets the location in the source code that is currently executed.
     * @param ip the location
     */
    void setCurrentLocation(Location ip);

    /**
     * @return the result of the script so far
     */
    JanitorObject getScriptResult();

    /**
     * Sets the result of the script.
     * @param scriptResult the result
     */
    void setScriptResult(JanitorObject scriptResult);

    /**
     * Emits a trace message, if tracing is enabled.
     * @param traceMessageSupplier supplies the message; it is only called if the message is needed
     */
    default void trace(Supplier<String> traceMessageSupplier) {
        getRuntime().trace(traceMessageSupplier);
    }

    /**
     * @return the locations of the calls that led to the current location
     */
    List<Location> getStackTrace();

    /**
     * Makes a module scope the current module scope.
     * @param moduleScope the scope
     */
    void pushModuleScope(Scope moduleScope);

    /**
     * Removes a module scope that was pushed earlier.
     * @param moduleScope the scope
     */
    void popModuleScope(Scope moduleScope);

    /**
     * Makes a closure scope available for lookups.
     * @param closureScope the scope
     */
    void pushClosureScope(Scope closureScope);

    /**
     * Removes a closure scope that was pushed earlier.
     * @param closureScope the scope
     */
    void popClosureScope(Scope closureScope);

    /**
     * Looks up a variable by name, starting in the current scope.
     * @param text the name of the variable
     * @return the value, or null if there is no such variable
     */
    JanitorObject lookup(String text);

    /**
     * Expand a template with arguments.
     *
     * @param template  the template to expand
     * @param arguments the arguments to expand with
     * @return the expanded template
     * @throws JanitorRuntimeException on errors
     */
    JString expandTemplate(JString template, JCallArgs arguments) throws JanitorRuntimeException;

    /**
     * Runs the script.
     * @return the result of the script
     * @throws JanitorRuntimeException if the script fails
     */
    @NotNull
    JanitorObject run() throws JanitorRuntimeException;

    /**
     * @return the environment of the runtime
     */
    @NotNull
    default JanitorEnvironment getEnvironment() {
        return getRuntime().getEnvironment();
    }

    /**
     * @return the built-in types of the environment
     */
    @NotNull
    default BuiltinTypes getBuiltins() {
        return getEnvironment().getBuiltinTypes();
    }

    /**
     * @return the formatting rules of the environment
     */
    @NotNull
    default JanitorFormatting getFormatting() {
        return getEnvironment().getFormatting();
    }

    /**
     * Run script code without throwing a script runtime exception on errors.
     * The environment may report an exception, but it may not throw.
     *
     * @param title a name for the protected code block, shown in an exception report
     * @param call  the code to execute
     */
    default void protect(final String title, ProtectedCall call) {
        getRuntime().protect(title, call);
    }

    /**
     * When scripts create objects that should be cleaned up after a script terminates, this method receives them.
     *
     * @param cleanable the object to clean up
     */
    void registerCleanable(JanitorCleanupRequired cleanable);

    /**
     * Internal method to count the number of instructions executed.
     *
     * @throws JanitorRuntimeException when exceeded
     */
    void countInstruction() throws JanitorRuntimeException;

    /**
     * Limits the number of instructions that the script may execute.
     * @param maxInstructionCount the maximum number, or 0 for no limit
     */
    void setMaxInstructionCount(final long maxInstructionCount);

    @FunctionalInterface
    interface ProtectedCall {
        /**
         * Runs the protected code.
         * @throws JanitorRuntimeException if the code fails
         */
        void call() throws JanitorRuntimeException;
    }

}
