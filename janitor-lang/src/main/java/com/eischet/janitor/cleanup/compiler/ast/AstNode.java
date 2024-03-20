package com.eischet.janitor.cleanup.compiler.ast;

import com.eischet.janitor.cleanup.api.api.scopes.Location;

public abstract class AstNode implements Ast {

    private final Location location;

    public AstNode(final Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public String toString() {
        return getClass().getSimpleName() + "@" + location;
    }


}
