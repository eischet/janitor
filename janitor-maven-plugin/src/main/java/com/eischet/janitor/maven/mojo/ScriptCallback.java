package com.eischet.janitor.maven.mojo;

import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.functions.JCallable;

/** A function from a script that is called later, together with the scope that it was registered in. */
public class ScriptCallback {
    private final JCallable callable;
    private final Scope scope;

    public ScriptCallback(final JCallable callable, final Scope scope) {
        this.callable = callable;
        this.scope = scope;
    }

    /**
     * @return the function to call
     */
    public JCallable getCallable() {
        return callable;
    }

    /**
     * @return the scope that the function was registered in
     */
    public Scope getScope() {
        return scope;
    }
}
