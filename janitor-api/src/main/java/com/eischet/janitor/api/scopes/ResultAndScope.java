package com.eischet.janitor.api.scopes;

import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper around a script result and a scope, used for reusing the scope.
 */
public class ResultAndScope {
    private final Scope scope;
    private final JanitorObject variable;

    public ResultAndScope(final @NotNull Scope scope, final @NotNull JanitorObject variable) {
        this.scope = scope;
        this.variable = variable;
    }

    public Scope getScope() {
        return scope;
    }

    public JanitorObject getVariable() {
        return variable;
    }
}
