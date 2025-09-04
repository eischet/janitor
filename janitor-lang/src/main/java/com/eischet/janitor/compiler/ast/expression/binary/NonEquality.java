package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Non-equality comparison of operands: !=.
 */
public class NonEquality extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public NonEquality(final Location location, final Expression left, final Expression right) {
        super(location, left, right, (leftValue, rightValue, rightValue2) -> Janitor.Semantics.areNotEquals(rightValue, rightValue2));
    }
}
