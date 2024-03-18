package com.eischet.janitor.compiler.ast.expression.literal;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;

public abstract class Literal extends AstNode implements Expression {

    public Literal(final Location location) {
        super(location);
    }

}
