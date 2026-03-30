package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.expression.Expression;

public class CaseInsensitiveEquality extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public CaseInsensitiveEquality(final Location location, final Expression left, final Expression right) {
        super(location, left, right, (process, leftValue, rightValue) -> Janitor.Semantics.areCaseInsensitiveEquals(leftValue, rightValue));
    }
}
