package com.eischet.janitor.cleanup.compiler.ast.expression.unary;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.AstNode;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.runtime.JanitorSemantics;

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
