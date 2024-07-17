package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Less than comparison of operands: &lt;.
 */
public class LessThan extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public LessThan(final Location location, final Expression left, final Expression right) {
        super(location, left, right, JanitorSemantics::lessThan);
    }
}
