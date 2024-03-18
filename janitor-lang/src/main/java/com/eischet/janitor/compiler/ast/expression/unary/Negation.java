package com.eischet.janitor.compiler.ast.expression.unary;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.runtime.JanitorSemantics;

public class Negation extends AstNode implements Expression {
    private final Expression expr;

    public Negation(final Location location, final Expression expr) {
        super(location);
        this.expr = expr;
    }


    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        return JanitorSemantics.negate(runningScript, expr.evaluate(runningScript).janitorUnpack());
        //return runningScript.getRuntime().negate(runningScript, expr.evaluate(runningScript).janitorUnpack());
    }
}
