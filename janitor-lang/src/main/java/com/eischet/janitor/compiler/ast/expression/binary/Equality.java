package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Equality comparison of operands: ==.
 */
public class Equality extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public Equality(final Location location, final Expression left, final Expression right) {
        super(location, left, right, (process, leftValue, rightValue) -> JanitorSemantics.areEquals(leftValue, rightValue));
    }
}
