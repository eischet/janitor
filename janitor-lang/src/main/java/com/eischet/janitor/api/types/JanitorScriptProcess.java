package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.scope.Scope;
import com.eischet.janitor.runtime.scope.ScopedVar;
import org.eclipse.collections.api.list.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface JanitorScriptProcess {

    void warn(final String warning);

    String getSource();

    Scope getMainScope();

    @NotNull Scope getCurrentScope();

    JanitorRuntime getRuntime();

    void enterBlock(final Location location);

    void exitBlock();

    ScopedVar lookupScopedVar(String id);

    void setScriptResult(JanitorObject scriptResult);

    Location getCurrentLocation();

    void setCurrentLocation(Location ip);

    JanitorObject getScriptResult();

    default void trace(Supplier<String> traceMessageSupplier) {
        getRuntime().trace(traceMessageSupplier);
    }

    ImmutableList<Location> getStackTrace();

    void pushModuleScope(Scope moduleScope);

    void popModuleScope(Scope moduleScope);

    JanitorObject lookup(String text);
}
