package com.eischet.janitor.compiler.ast.statement.specops;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.Identifier;
import com.eischet.janitor.compiler.ast.statement.Statement;

public abstract class AnyFixOperator extends Statement implements Expression {
    private final Expression expr;

    public AnyFixOperator(final Location location, final Expression expr) {
        super(location);
        this.expr = expr;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        runningScript.setCurrentLocation(getLocation());
        runningScript.trace(() -> this + " : expr=" + expr);
        if (expr instanceof Identifier) {
            final String id = ((Identifier) expr).getText();
            final ResultAndScope scoped = runningScript.lookupScopedVar(id);
            if (scoped == null) {
                throw new JanitorArgumentException(runningScript, "variable not bound: {}. cannot apply " + this + " to it.");
            }
            final JanitorObject currentValue = scoped.getVariable().janitorUnpack(); // oder das?? final Variable currentValue = expr.evaluate(runningScript);
            final JanitorObject newValue = operate(runningScript, currentValue);
            scoped.getScope().bind(runningScript, id, newValue);  // vorher falsch: runningScript.getCurrentScope().bind(id, newValue);
            return pick(currentValue, newValue);
        } else {
            throw new JanitorArgumentException(runningScript, "cannot apply " + this + " to " + expr);
        }
    }

    protected abstract JanitorObject pick(final JanitorObject currentValue, final JanitorObject newValue);

    protected abstract JanitorObject operate(final JanitorScriptProcess runningScript, final JanitorObject currentValue) throws JanitorRuntimeException;

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        runningScript.setCurrentLocation(getLocation());
        evaluate(runningScript); // just pass it on
    }

}
