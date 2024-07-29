package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Logic OR of operands: or.
 * Deprecated but usable: ||.
 */
public class LogicOr extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public LogicOr(final Location location, final Expression left, final Expression right) {
        super(location, left, right, (leftValue, rightValue, rightValue2) -> JanitorSemantics.logicOr(rightValue, rightValue2));
    }
}
