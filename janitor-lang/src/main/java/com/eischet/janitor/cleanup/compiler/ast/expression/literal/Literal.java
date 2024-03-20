package com.eischet.janitor.cleanup.compiler.ast.expression.literal;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.compiler.ast.AstNode;

public abstract class Literal extends AstNode implements Expression {

    public Literal(final Location location) {
        super(location);
    }

}
