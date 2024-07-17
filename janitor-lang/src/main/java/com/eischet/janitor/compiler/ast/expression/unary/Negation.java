package com.eischet.janitor.compiler.ast.expression.unary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;

/**
 * Negation of an expression: -.
 * As in -17, not a-b, which is a binary subtraction operation.
 */
public class Negation extends AstNode implements Expression {
    private final Expression expr;

    /**
     * Constructor.
     *
     * @param location where
     * @param expr     what
     */
    public Negation(final Location location, final Expression expr) {
        super(location);
        this.expr = expr;
    }


    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return JanitorSemantics.negate(runningScript, expr.evaluate(runningScript).janitorUnpack());
    }
}
