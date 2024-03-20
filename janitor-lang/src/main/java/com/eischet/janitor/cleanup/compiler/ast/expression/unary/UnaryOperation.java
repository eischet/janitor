package com.eischet.janitor.cleanup.compiler.ast.expression.unary;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.compiler.ast.AstNode;

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
