package com.eischet.janitor.maven.mojo;

import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.functions.JCallable;

public class ScriptCallback {
    private final JCallable callable;
    private final Scope scope;

    public ScriptCallback(final JCallable callable, final Scope scope) {
        this.callable = callable;
        this.scope = scope;
    }

    public JCallable getCallable() {
        return callable;
    }

    public Scope getScope() {
        return scope;
    }
}
