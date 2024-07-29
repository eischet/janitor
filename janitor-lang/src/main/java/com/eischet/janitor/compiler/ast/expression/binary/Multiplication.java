package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Multiplication of operands: *.
 */
public class Multiplication extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public Multiplication(final Location location, final Expression left, final Expression right) {
        super(location, left, right, JanitorSemantics::multiply);
    }
}
