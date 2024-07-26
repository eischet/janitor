package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Logical AND of operands: and.
 * Deprecated but usable: &amp;&amp;.
 * This is slightly different from other binary operations in that it short-circuits.
 */
public class LogicAnd extends AstNode implements Expression {
    private final Expression left;
    private final Expression right;

    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
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
