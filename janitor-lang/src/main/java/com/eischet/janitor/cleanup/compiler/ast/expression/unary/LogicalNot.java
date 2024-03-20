package com.eischet.janitor.cleanup.compiler.ast.expression.unary;

import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.runtime.JanitorSemantics;

public class LogicalNot extends UnaryOperation {
    public LogicalNot(final Location location, final Expression parameter) {
        super(location, parameter, (parameter1, parameter12) -> JanitorSemantics.logicNot(parameter12));
    }
}
