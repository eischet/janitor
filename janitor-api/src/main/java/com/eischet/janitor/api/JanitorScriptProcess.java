package com.eischet.janitor.api;

import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Represents a script that is running right now.
 * This is used mainly by the interpreter, to actually run the script, but also by host code that needs to interact with a script
 * or do things for the script which require access to the interpreter's internals. In the latter context, this interface can
 * be seen as a "handle" to the interpreter.
 */
public interface JanitorScriptProcess {

    /**
     * Emit a warning.
     * <p>
     * What this does depends on the implementation. Typically, this should log a message
     * with the warning level.
     * </p>
     *
     * @param warning the warning
     */
    void warn(final String warning);

    /**
     * Returns a name for the process,
     * @return
     */
    @NotNull String getProcessName();

    /**
     * Retrieve the source code, if available, of the currently running script's main module.
     * @return source code
     */
    @Nullable String getSource();

    Scope getMainScope();

    @NotNull Scope getCurrentScope();

    JanitorRuntime getRuntime();

    void enterBlock(final Location location);

    void exitBlock();

    ResultAndScope lookupScopedVar(String id);

    void setScriptResult(JanitorObject scriptResult);

    Location getCurrentLocation();

    void setCurrentLocation(Location ip);

    JanitorObject getScriptResult();

    default void trace(Supplier<String> traceMessageSupplier) {
        getRuntime().trace(traceMessageSupplier);
    }

    List<Location> getStackTrace();

    void pushModuleScope(Scope moduleScope);

    void popModuleScope(Scope moduleScope);

    void pushClosureScope(Scope closureScope);

    void popClosureScope(Scope closureScope);

    JanitorObject lookup(String text);

    /**
     * Expand a template with arguments.
     * @param template the template to expand
     * @param arguments the arguments to expand with
     * @return the expanded template
     * @throws JanitorRuntimeException on errors
     */
    JString expandTemplate(JString template, JCallArgs arguments) throws JanitorRuntimeException;

    @NotNull
    JanitorObject run() throws JanitorRuntimeException;

    @NotNull
    default JanitorEnvironment getEnvironment() {
        return getRuntime().getEnvironment();
    }

    @NotNull
    default BuiltinTypes getBuiltins() {
        return getEnvironment().getBuiltinTypes();
    }

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

    @FunctionalInterface
    interface ProtectedCall {
        void call() throws JanitorRuntimeException;
    }

}
