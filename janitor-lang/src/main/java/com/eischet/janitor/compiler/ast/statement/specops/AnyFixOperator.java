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

/**
 * Abstract base class for any fix operator (prefix or postfix).
 * You might have noticed that most operators are statements, not expressions.
 * These operators are expressions, to, so you can use them in other expressions: foo = bar++.
 * However, you cannot say this in Janitor: foo = bar += 1
 */
public abstract class AnyFixOperator extends Statement implements Expression {
    private final Expression expr;

    /**
     * Constructor.
     *
     * @param location where
     * @param expr     what to operate on
     */
    public AnyFixOperator(final Location location, final Expression expr) {
        super(location);
        this.expr = expr;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.setCurrentLocation(getLocation());
        process.trace(() -> this + " : expr=" + expr);
        if (expr instanceof Identifier) {
            final String id = ((Identifier) expr).getText();
            final ResultAndScope scoped = process.lookupScopedVar(id);
            if (scoped == null) {
                throw new JanitorArgumentException(process, "variable not bound: {}. cannot apply " + this + " to it.");
            }
            final JanitorObject currentValue = scoped.getVariable().janitorUnpack(); // oder das?? final Variable currentValue = expr.evaluate(runningScript);
            final JanitorObject newValue = operate(process, currentValue);
            scoped.getScope().bind(process, id, newValue);  // vorher falsch: runningScript.getCurrentScope().bind(id, newValue);
            return pick(currentValue, newValue);
        } else {
            throw new JanitorArgumentException(process, "cannot apply " + this + " to " + expr);
        }
    }

    /**
     * Pick the value to return.
     * Some operators return the previous value, e.g. i++, some return the new value, e.g. ++i.
     *
     * @param currentValue current value
     * @param newValue     new value
     * @return whatever is appropriate for the operator
     */
    protected abstract JanitorObject pick(final JanitorObject currentValue, final JanitorObject newValue);

    /**
     * Apply the operator.
     *
     * @param process script
     * @param currentValue  current value
     * @return new value
     * @throws JanitorRuntimeException on errors
     */
    protected abstract JanitorObject operate(final JanitorScriptProcess process, final JanitorObject currentValue) throws JanitorRuntimeException;

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        process.setCurrentLocation(getLocation());
        evaluate(process); // just pass it on
    }

}
