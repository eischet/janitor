package com.eischet.janitor.compiler.ast.statement.specops;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.Identifier;
import com.eischet.janitor.compiler.ast.statement.Statement;

public class PostfixIncrement extends Statement implements Expression {
    private final Expression expr;

    public PostfixIncrement(final Location location, final Expression expr) {
        super(location);
        this.expr = expr;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
        runningScript.setCurrentLocation(getLocation());
        runningScript.trace(() -> "postfix increment: expr=" + expr);
        if (expr instanceof Identifier) {
            final String id = ((Identifier) expr).getText();
            final ResultAndScope scoped = runningScript.lookupScopedVar(id);
            if (scoped == null) {
                throw new JanitorArgumentException(runningScript, "variable not bound: {}. cannot apply postfix++ to it.");
            }
            final JanitorObject currentValue = scoped.getVariable(); // oder das?? final Variable currentValue = expr.evaluate(runningScript);
            final JanitorObject newValue = JanitorSemantics.increment(runningScript, currentValue);
            scoped.getScope().bind(runningScript, id, newValue);  // vorher falsch: runningScript.getCurrentScope().bind(id, newValue);
            return currentValue;
        } else {
            throw new JanitorArgumentException(runningScript, "cannot apply postfix++ to " + expr);
        }
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        evaluate(runningScript); // just pass it on
    }
}
