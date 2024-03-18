package com.eischet.janitor.compiler.ast.expression.unary;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;

public abstract class UnaryOperation extends AstNode implements Expression {
    protected final Expression parameter;
    private final UnaryOperationDelegate functor;

    protected UnaryOperation(final Location location, final Expression parameter, final UnaryOperationDelegate functor) {
        super(location);
        this.parameter = parameter;
        this.functor = functor;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        final JanitorObject variable = parameter.evaluate(runningScript).janitorUnpack();
        runningScript.setCurrentLocation(getLocation());
        return functor.perform(runningScript, variable);
    }

}
