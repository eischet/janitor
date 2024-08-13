package com.eischet.janitor.compiler.ast;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.toolbox.json.api.JsonExportable;

/**
 * Base class for all AST nodes, which are either Statements or Expressions.
 * @see com.eischet.janitor.compiler.ast.expression.Expression
 * @see com.eischet.janitor.compiler.ast.statement.Statement
 */
public abstract class AstNode implements Ast, JsonExportable {

    private final Location location;

    /**
     * Create a new AST node at the specified location.
     * @param location the location of the corresponding instruction.
     */
    public AstNode(final Location location) {
        this.location = location;
    }

    /**
     * Return the location of this AST node.
     * @return the location of this AST node.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Return a string representation of the AST node, which might not be readable code.
     * @return a string representation of the AST node
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + location;
    }

    /**
     * For JSON export purposes, neve omit any part of the AST.
     * @return false
     */
    @Override
    public boolean isDefaultOrEmpty() {
        return false;
    }
}
