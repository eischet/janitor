package com.eischet.janitor.api;

import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * Represents a script that is running right now.
 * This is used mainly by the interpreter, to actually run the script, but also by host code that needs to interact with a script
 * or do things for the script which require access to the interpreter's internals. In the latter context, this interface can
 * be seen as a "handle" to the interpreter.
 */
public interface JanitorScriptProcess {

    void warn(final String warning);

    String getSource();

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

    JString expandTemplate(JString template, JCallArgs arguments) throws JanitorRuntimeException;

    @NotNull
    JanitorObject run() throws JanitorRuntimeException;

    @NotNull
    default JanitorEnvironment getEnvironment() {
        return getRuntime().getEnvironment();
    }

    @NotNull
    default JanitorBuiltins getBuiltins() {
        return getEnvironment().getBuiltins();
    }

    /**
     * Run script code (i.e. code that can throw JanitorRuntimeException).
     *
     * @param title
     * @param call
     */
    default void protect(final String title, ProtectedCall call) {
        getRuntime().protect(title, call);
    }

    JFloat requireFloat(Object value) throws JanitorArgumentException;

    @FunctionalInterface
    interface ProtectedCall {
        void call() throws JanitorRuntimeException;
    }

}
