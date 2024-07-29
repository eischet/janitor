package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Greater than or equals comparison of operands: &gt;=.
 */
public class GreaterThanOrEquals extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public GreaterThanOrEquals(final Location location, final Expression left, final Expression right) {
        super(location, left, right, JanitorSemantics::greaterThanOrEquals);
    }
}
