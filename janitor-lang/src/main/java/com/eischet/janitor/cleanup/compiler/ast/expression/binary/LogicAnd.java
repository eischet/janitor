package com.eischet.janitor.cleanup.compiler.ast.expression.binary;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JBool;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.compiler.ast.AstNode;
import com.eischet.janitor.cleanup.runtime.JanitorSemantics;

public class LogicAnd extends AstNode implements Expression {
    private final Expression left;
    private final Expression right;

    public LogicAnd(final Location location, final Expression left, final Expression right) {
        super(location);
        this.left = left;
        this.right = right;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        runningScript.setCurrentLocation(getLocation());
        final JanitorObject leftValue = left.evaluate(runningScript).janitorUnpack();
        if (!JanitorSemantics.isTruthy(leftValue)) {
            return JBool.FALSE;
        }
        final JanitorObject rightValue = right.evaluate(runningScript).janitorUnpack();
        return JBool.map(JanitorSemantics.isTruthy(rightValue));
    }
}
