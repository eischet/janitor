package com.eischet.janitor.runtime;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.scope.Scope;
import org.jetbrains.annotations.NotNull;

public class ResultAndScope {
    private final Scope scope;
    private final JanitorObject result;

    public ResultAndScope(final @NotNull Scope scope, final @NotNull JanitorObject result) {
        this.scope = scope;
        this.result = result;
    }

    public Scope getScope() {
        return scope;
    }

    public JanitorObject getResult() {
        return result;
    }
}
