package com.eischet.janitor.runtime.scope;

import com.eischet.janitor.api.types.JanitorObject;

public class ScopedVar {
    private final Scope scope;
    private final JanitorObject variable;

    public ScopedVar(final Scope scope, final JanitorObject variable) {
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