package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Greater than comparison of operands: &gt;.
 */
public class GreaterThan extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public GreaterThan(final Location location, final Expression left, final Expression right) {
        super(location, left, right, JanitorSemantics::greaterThan);
    }
}
