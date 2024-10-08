package com.eischet.janitor.compiler.ast.expression.unary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Logical NOT operation: not.
 */
public class LogicalNot extends UnaryOperation {
    public LogicalNot(final Location location, final Expression parameter) {
        super(location, parameter, (parameter1, parameter12) -> JanitorSemantics.logicNot(parameter12));
    }

}
