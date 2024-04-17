package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

public class Multiplication extends BinaryOperation {
    public Multiplication(final Location location, final Expression left, final Expression right) {
        super(location, left, right, JanitorSemantics::multiply);
    }
}
