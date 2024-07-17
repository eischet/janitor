package com.eischet.janitor.compiler.ast;

import com.eischet.janitor.api.scopes.Location;

/**
 * Base class for all AST nodes, which are either Statements or Expressions.
 * @see com.eischet.janitor.compiler.ast.expression.Expression
 * @see com.eischet.janitor.compiler.ast.statement.Statement
 */
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
