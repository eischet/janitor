package com.eischet.janitor.compiler.ast;

import com.eischet.janitor.api.scopes.Location;

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
