package com.eischet.janitor.cleanup.compiler.ast.expression.binary;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.runtime.JanitorSemantics;

public class Equality extends BinaryOperation {
    public Equality(final Location location, final Expression left, final Expression right) {
        super(location, left, right, (process, leftValue, rightValue) -> JanitorSemantics.areEquals(leftValue, rightValue));
    }
}
