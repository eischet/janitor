package com.eischet.janitor.compiler.ast.statement;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.controlflow.JanitorControlFlowException;

public class ExpressionStatement extends Statement {

    private final Expression expression;

    public ExpressionStatement(final Location location, final Expression expression) {
        super(location);
        this.expression = expression;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        runningScript.setCurrentLocation(getLocation());
        runningScript.setScriptResult(expression.evaluate(runningScript).janitorUnpack());
    }
}