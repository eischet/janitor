package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Subtraction of operands: -.
 */
public class Subtraction extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public Subtraction(final Location location, final Expression left, final Expression right) {
        super(location, left, right, JanitorSemantics::subtract);
    }
}
