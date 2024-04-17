package com.eischet.janitor.api;

import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JString;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

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

    JanitorObject lookup(String text);

    JanitorObject lookupClassAttribute(JanitorObject instance, String attributeName);

    JString expandTemplate(JString template, JCallArgs arguments) throws JanitorRuntimeException;

    JanitorObject run() throws JanitorRuntimeException;
}
