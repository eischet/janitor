package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import org.jetbrains.annotations.NotNull;

/**
 * Literals, that is: values defined in the source code, like "foo" or 17.
 * These evaluate to themselves, usually.
 */
public abstract class Literal extends AstNode implements Expression {

    /**
     * Constructor.
     * @param location where
     */
    public Literal(final @NotNull Location location) {
        super(location);
    }

}
