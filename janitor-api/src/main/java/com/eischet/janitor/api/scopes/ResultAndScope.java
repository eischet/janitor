package com.eischet.janitor.api.scopes;

import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper around a script result and a scope, used for reusing the scope and getting the result in one call.
 * Turns out it's quite useful to keep the scope of a script around, and feed it into another script later.
 */
public class ResultAndScope {
    private final Scope scope;
    private final JanitorObject variable;

    /**
     * Create a new ResultAndScope.
     * @param scope the scope
     * @param variable the result
     */
    public ResultAndScope(final @NotNull Scope scope, final @NotNull JanitorObject variable) {
        this.scope = scope;
        this.variable = variable;
    }

    /**
     * Get the scope.
     * @return the scope
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Get the result.
     * @return the result
     */
    public JanitorObject getVariable() {
        return variable;
    }
}
