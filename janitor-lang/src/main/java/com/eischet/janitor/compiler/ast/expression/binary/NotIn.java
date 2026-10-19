// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Negated membership test of operands: not in.
 */
public class NotIn extends BinaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param left left operand, the element
     * @param right right operand, the container
     */
    public NotIn(final Location location, final Expression left, final Expression right) {
        super(location, left, right, JanitorSemantics::isNotIn);
    }
}
